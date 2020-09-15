package net.nprod.onpdb.wdimport

import net.nprod.onpdb.goldcleaner.loadGoldData
import net.nprod.onpdb.mock.TestISparql
import net.nprod.onpdb.mock.TestPublisher
import net.nprod.onpdb.wdimport.wd.*
import net.nprod.onpdb.wdimport.wd.models.WDArticle
import net.nprod.onpdb.wdimport.wd.models.WDCompound
import net.nprod.onpdb.wdimport.wd.models.WDTaxon
import net.nprod.onpdb.wdimport.wd.sparql.ISparql
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql
import org.apache.logging.log4j.LogManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.RDFHandler
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.sail.memory.MemoryStore
import java.io.File
import java.io.FileNotFoundException


//const val PREFIX = "/home/bjo/Store/01_Research/opennaturalproductsdb"
const val PREFIX = "/home/bjo/Software/ONPDB/opennaturalproductsdb"
const val GOLD_PATH = "$PREFIX/data/interim/tables/4_analysed/gold.tsv.gz"

const val TestMode = true
const val TestPersistent = true // If true, we are going to reload the previous output in TestMode

fun main(args: Array<String>) {
    val logger = LogManager.getLogger("net.nprod.onpdb.wdimport.main")
    logger.info("ONPDB Importer")
    if (TestMode) {
        logger.info("We are in test mode")
    }


    // This is where we say if we use the test Wikidata instance or not
    // the issue is that the test wikidata doesn't have sparql, so it is
    // harder for us to find if something already exist
    val instanceItems = MainInstanceItems

    logger.info("Initializing toolkit")
    /*val wikibaseDataFetcher = WikibaseDataFetcher(
        BasicApiConnection.getTestWikidataApiConnection(), // TODO put that in instance as well
        "http://test.wikidata.org/entity/"
    )*/
/*
    logger.info("Fetching data for something")
    val thing = wikibaseDataFetcher.getEntityDocument("Q212578")
    println(thing)*/

    lateinit var wdSparql: ISparql
    lateinit var publisher: Publisher
    var repository: Repository? = null
    if (TestMode) {
        repository = SailRepository(MemoryStore())
        if (TestPersistent) {
            try {
                logger.info("Loading old data")
                val file = File("/tmp/out.xml")
                repository.let {
                    it.connection
                    it.connection.add(file.inputStream(), "", RDFFormat.RDFXML)
                }
            } catch (e: FileNotFoundException) {
                logger.info("There is no data from a previous test run.")
            }
        }
        wdSparql = TestISparql(instanceItems, repository)
        publisher = TestPublisher(instanceItems, repository)
    } else {
        wdSparql = WDSparql(instanceItems)
        publisher = WDPublisher(instanceItems)
    }

    publisher.connect()

    val dataTotal = loadGoldData(GOLD_PATH, 100000)

    logger.info("Producing organisms")


    val organisms = dataTotal.organismCache.store.values.mapNotNull { organism ->

        logger.info("Organism Ranks and Ids: ${organism.rankIds}")

        var taxon: WDTaxon? = null


        listOfNotNull("GBIF", "NCBI", "ITIS", "The Interim Register of Marine and Nonmarine Genera",
        "World Register of Marine Species", "GBIF Backbone Taxonomy").forEach { taxonDbName ->
            val taxonDb = organism.rankIds.keys.firstOrNull { it.name == taxonDbName }
            if (taxon != null) return@forEach
            taxonDb?.let {
                val genus = organism.rankIds[taxonDb]?.firstOrNull { it.first == "genus" }?.second?.name
                val species = organism.rankIds[taxonDb]?.firstOrNull { it.first == "species" }?.second?.name

                val genusId = organism.rankIds[taxonDb]?.firstOrNull { it.first == "genus" }?.second?.id
                val speciesId = organism.rankIds[taxonDb]?.firstOrNull { it.first == "species" }?.second?.id

                if (genus == null) throw Exception("Sorry we need at least a genus for organism $organism")

                val genusWD = WDTaxon(
                    name = genus,
                    parentTaxon = null,
                    taxonName = genus,
                    taxonRank = InstanceItems::genus
                ).tryToFind(wdSparql, instanceItems)



                taxon = genusWD
                if (species != null) {
                    publisher.publish(genusWD, "Created a missing genus")

                    val speciesWD = WDTaxon(
                        name = species,
                        parentTaxon = genusWD.id,
                        taxonName = species,
                        taxonRank = InstanceItems::species
                    ).tryToFind(wdSparql, instanceItems)
                    taxon = speciesWD

                }


            }
        }

        if (taxon == null) {
            throw Exception("Sorry we couldn't find any info from the accepted reference taxonomy source, we only have: ${organism.rankIds.keys.map { it.name}}")
        }


        taxon?.let { taxon ->


            // TODO get that to work
            organism.textIds.forEach { dbEntry ->
                taxon.addTaxoDB(dbEntry.key, dbEntry.value)
            }

            publisher.publish(taxon, "Created a missing taxon")
            organism to taxon
        }

    }.toMap()

    logger.info("Producing articles")

    val references = dataTotal.referenceCache.store.map {
        val article = WDArticle(
            name = "No title yet…",
            title = it.value.doi, // TODO: get the titles
            doi = it.value.doi,
        )
        // TODO: Add PMID and PMCID
        publisher.publish(article, "Creating a new article")
        it.value to article
    }.toMap()

    logger.info("Linking")

    dataTotal.compoundCache.store.forEach { (id, compound) ->
        val wdcompound = WDCompound(
            name = compound.inchikey,
            inChIKey = compound.inchikey,
            inChI = compound.inchi,
            isomericSMILES = compound.inchi,
            pcId = "TODO", // TODO: Export PCID
            chemicalFormula = "TODO" // TODO: Calculate chemical formula
        ) {
            dataTotal.quads.filter { it.compound == compound }.distinct().forEach { quad ->
                val organism = organisms[quad.organism]
                organism?.let {
                    naturalProductOfTaxon(
                        organism
                    ) {
                    statedIn(
                        references[quad.reference]?.id
                            ?: throw Exception("That's bad we talk about a reference we don't have.")
                    )
                }
                }
            }
        }

        publisher.publish(wdcompound, "Creating a new compound")
    }

    publisher.disconnect()

    if (TestMode) {
        val file = File("/tmp/out.xml").bufferedWriter()
        repository?.let {
            it.connection
            val writer: RDFHandler = Rio.createWriter(RDFFormat.RDFXML, file)
            it.connection.export(writer)
        }
        file.close()
    }
}
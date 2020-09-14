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


//const val PREFIX = "/home/bjo/Store/01_Research/opennaturalproductsdb"
const val PREFIX = "/home/bjo/Software/ONPDB/opennaturalproductsdb"
const val GOLD_PATH = "$PREFIX/data/interim/tables/4_analysed/gold.tsv.gz"

const val TestMode = true

fun main(args: Array<String>) {
    val logger = LogManager.getLogger("net.nprod.onpdb.wdimport.main")
    logger.info("ONPDB Importer")
    if (TestMode) {
        logger.info("We are in test mode")
    }

    val instanceItems = TestInstanceItems

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
        wdSparql = TestISparql(MainInstanceItems, repository)
        publisher = TestPublisher(MainInstanceItems, repository)
    } else {
        wdSparql = WDSparql(MainInstanceItems) // TODO: For tests we use the official…
        publisher = WDPublisher(instanceItems)
    }

    publisher.connect()

    val dataTotal = loadGoldData(GOLD_PATH, 10000)

    logger.info("Producing organisms")

    val organisms = dataTotal.organismCache.store.values.mapNotNull { organism ->
        val organismSplit = organism.name.split(" ")
        val taxon: WDTaxon? = when (organismSplit.size) {
            0 -> throw Exception("Empty organism name")
            1 -> {
                val genusWD = WDTaxon(
                    name = organismSplit[0],
                    parentTaxon = null,
                    taxonName = organismSplit[0],
                    taxonRank = InstanceItems::genus
                ).tryToFind(wdSparql, instanceItems)

                genusWD
            }
            2-> {
                val genusWD = WDTaxon(
                    name = organismSplit[0],
                    parentTaxon = null,
                    taxonName = organismSplit[0],
                    taxonRank = InstanceItems::genus
                ).tryToFind(wdSparql, instanceItems)

                publisher.publish(genusWD, "Created a missing genus")

                val speciesWD = WDTaxon(
                    name = organism.name,
                    parentTaxon = genusWD.id,
                    taxonName = organismSplit[1],
                    taxonRank = InstanceItems::species
                )

                speciesWD
            }
            else -> {
                logger.error("More than 2 elements in taxon not handled yet for: ${organism.name}")
                null
            }
        }
        taxon?.let {
            organism.textIds.forEach { dbEntry ->
                taxon.addTaxoDB(dbEntry.key, dbEntry.value)
            }

            // Todo, add the taxinfo

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
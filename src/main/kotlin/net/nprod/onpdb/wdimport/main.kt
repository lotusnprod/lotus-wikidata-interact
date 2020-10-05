package net.nprod.onpdb.wdimport

import net.nprod.onpdb.chemistry.smilesToFormula
import net.nprod.onpdb.input.DataTotal
import net.nprod.onpdb.input.Organism
import net.nprod.onpdb.input.loadData
import net.nprod.onpdb.mock.TestISparql
import net.nprod.onpdb.mock.TestPublisher
import net.nprod.onpdb.wdimport.wd.*
import net.nprod.onpdb.wdimport.wd.interfaces.Publisher
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
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher
import java.io.File
import java.io.FileNotFoundException


//const val PREFIX = "/home/bjo/Store/01_Research/opennaturalproductsdb"
const val PREFIX = "/home/bjo/Software/ONPDB/opennaturalproductsdb"

//const val DATASET_PATH = "$PREFIX/data/interim/tables/4_analysed/platinum.tsv.gz"
const val DATASET_PATH = "data/manuallyValidated.tsv"
const val REPOSITORY_FILE = "/tmp/out.xml"

const val TestMode = false
const val TestPersistent = false // If true, we are going to reload the previous output in TestMode
const val TestExport = true // If true export a dump of the RDF repository in REPOSITORY_FILE
const val TestRealSparql = true // If true, we test with the real WikiData sparql

fun main(args: Array<String>) {
    val logger = LogManager.getLogger("net.nprod.onpdb.chemistry.main")
    logger.info("ONPDB Importer")
    if (TestMode) {
        logger.info("We are in test mode")
    }

    // This is where we say if we use the test Wikidata instance or not
    // the issue is that the test wikidata doesn't have sparql, so it is
    // harder for us to find if something already exist
    val instanceItems = MainInstanceItems

    logger.info("Initializing toolkit")

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
        wdSparql = if (TestRealSparql) {
            WDSparql(instanceItems)
        } else {
            TestISparql(instanceItems, repository)
        }
        publisher = TestPublisher(instanceItems, repository)
    } else {
        wdSparql = WDSparql(instanceItems)
        publisher = WDPublisher(instanceItems, pause = 10)
    }

    publisher.connect()

    val dataTotal = loadData(DATASET_PATH, 1)

    logger.info("Producing organisms")


    val organisms = findAllTaxonForOrganismFromCache(dataTotal, wdSparql, instanceItems, publisher)

    logger.info("Producing articles")

    val references = dataTotal.referenceCache.store.map {
        val article = WDArticle(
                name = it.value.title ?: it.value.doi,
                title = it.value.title,
                doi = it.value.doi,
        ).tryToFind(wdSparql, instanceItems)
        // TODO: Add PMID and PMCID
        publisher.publish(article, "Creating a new article")
        it.value to article
    }.toMap()

    logger.info("Linking")

    logger.info("Creating a local cache of wikidata ids for existing compounds")
    // We do that so we don't need to do hundreds of thousands of SPARQL queries
    val wikiCompoundCache = mutableMapOf<String, String>()
    val inchiKeys = dataTotal.compoundCache.store.map { (id, compound) ->
        compound.inchikey
    }
    inchiKeys.chunked(1000) { inchiKeysBlock ->
        repository?.let {
            val query = """
               SELECT ?id ?inchikey WHERE {
                   ?id <${instanceItems.inChIKey.iri}> ?inchikey.
                   VALUES ?inchikey {
                      ${inchiKeysBlock.joinToString(" ") { key -> Rdf.literalOf(key).queryString }}
                   }
                }
            """.trimIndent()
            logger.info(query)
            val o = wdSparql.query(query) { result ->
                result.forEach {
                    wikiCompoundCache[it.getValue("inchikey").stringValue()] = it.getValue("id").stringValue().split("/").last()
                }
            }
        }
    }

    // Adding all compounds

    dataTotal.compoundCache.store.forEach { (id, compound) ->
        val wdcompound = WDCompound(
                name = "",
                inChIKey = compound.inchikey,
                inChI = compound.inchi,
                isomericSMILES = compound.smiles,
                chemicalFormula = smilesToFormula(compound.smiles)
        ).tryToFind(wdSparql, instanceItems)

        wdcompound.apply {
            dataTotal.quads.filter { it.compound == compound }.distinct().forEach { quad ->
                val organism = organisms[quad.organism]
                organism?.let {
                    foundInTaxon(
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

// Counting

    if (TestMode) {
        repository?.let {
            val query = """
                SELECT * {
                   { SELECT (count (distinct ?org) as ?orgcount) WHERE {
                   ?org <${instanceItems.instanceOf.iri}> <${instanceItems.taxon.iri}>.
                   }
                   }
                   
                   { SELECT (count (distinct ?cpd) as ?cpdcount) WHERE {
                   ?cpd <${instanceItems.instanceOf.iri}> <${instanceItems.chemicalCompound.iri}>.
                   }
                   }
                }
            """.trimIndent()
            logger.info(query)
            val bindingSet = it.connection.prepareTupleQuery(query).evaluate().first()
            val orgcount = bindingSet.getBinding("orgcount").value.stringValue()
            val cpdcount = bindingSet.getBinding("cpdcount").value.stringValue()

            logger.info("We have $orgcount taxa and $cpdcount compounds in the local repository")
        }

    }
    logger.info("Publisher has made ${publisher.newDocuments} new documents and updated ${publisher.updatedDocuments}")

// Exporting

    if (TestMode && TestExport) {
        val file = File(REPOSITORY_FILE).bufferedWriter()
        repository?.let {
            it.connection
            val writer: RDFHandler = Rio.createWriter(RDFFormat.RDFXML, file)
            it.connection.export(writer)
        }
        file.close()
    }
}

fun findAllTaxonForOrganismFromCache(dataTotal: DataTotal, wdSparql: ISparql, instanceItems: InstanceItems, publisher: Publisher): Map<Organism, WDTaxon> {
    val logger = LogManager.getLogger("findAllTAxonForOrganismFromCache")
    return dataTotal.organismCache.store.values.mapNotNull { organism ->

        logger.debug("Organism Ranks and Ids: ${organism.rankIds}")

        var taxon: WDTaxon? = null

        listOf(
                "GBIF",
                "NCBI",
                "ITIS",
                "Index Fungorum",
                "The Interim Register of Marine and Nonmarine Genera",
                "World Register of Marine Species",
                "Database of Vascular Plants of Canada (VASCAN)",
                "GBIF Backbone Taxonomy"
        ).forEach { taxonDbName ->
            val taxonDb = organism.rankIds.keys.firstOrNull { it.name == taxonDbName }
            if (taxon != null) return@forEach
            taxonDb?.let {
                val genus = organism.rankIds[taxonDb]?.firstOrNull { it.first == "genus" }?.second?.name
                val species = organism.rankIds[taxonDb]?.firstOrNull { it.first == "species" }?.second?.name

                val genusId = organism.rankIds[taxonDb]?.firstOrNull { it.first == "genus" }?.second?.id
                val speciesId = organism.rankIds[taxonDb]?.firstOrNull { it.first == "species" }?.second?.id

                if (genus != null) {

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
        }

        if (taxon == null) {
            throw Exception("Sorry we couldn't find any info from the accepted reference taxonomy source, we only have: ${organism.rankIds.keys.map { it.name }}")
        }

        taxon?.let { taxon ->
            // TODO get that to work
            organism.textIds.forEach { dbEntry ->
                taxon.addTaxoDB(dbEntry.key, dbEntry.value.split("|").last())
            }

            publisher.publish(taxon, "Created a missing taxon")
            organism to taxon
        }

    }.toMap()
}

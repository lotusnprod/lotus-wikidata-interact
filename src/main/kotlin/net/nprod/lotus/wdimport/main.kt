package net.nprod.lotus.wdimport

import kotlinx.cli.*
import net.nprod.lotus.chemistry.smilesToFormula
import net.nprod.lotus.chemistry.subscriptFormula
import net.nprod.lotus.input.loadData
import net.nprod.lotus.wdimport.wd.mock.TestISparql
import net.nprod.lotus.wdimport.wd.*
import net.nprod.lotus.wdimport.wd.interfaces.Publisher
import net.nprod.lotus.wdimport.wd.models.WDArticle
import net.nprod.lotus.wdimport.wd.models.WDCompound
import net.nprod.lotus.wdimport.wd.query.WDKT
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import org.apache.logging.log4j.LogManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.RDFHandler
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import java.io.File
import java.io.FileNotFoundException


fun main(args: Array<String>) {
    val logger = LogManager.getLogger("net.nprod.lotus.chemistry.net.nprod.lotus.tools.wdpropcreator.main")
    val parser = ArgParser("lotus_importer")
    val input by parser.option(ArgType.String, shortName = "i", description = "Input file").required()
    val limit by parser.option(
        ArgType.Int,
        shortName = "l",
        description = "Limit the import to this number of entries (-1 for all, default 1)"
    ).default(1)
    val skip by parser.option(ArgType.Int, shortName = "s", description = "Skip this number of entries").default(0)
    val real by parser.option(
        ArgType.Boolean,
        shortName = "r",
        description = "Turn on real mode: this will write to WikiData!"
    ).default(false)
    val persistent by parser.option(
        ArgType.Boolean,
        shortName = "p",
        description = "Turn on persistent mode (only for tests)"
    ).default(false)
    val realSparql by parser.option(
        ArgType.Boolean,
        shortName = "S",
        description = "Use the real WikiData instance for SPARQL queries (only for tests)"
    ).default(false)
    val output by parser.option(
        ArgType.String,
        shortName = "o",
        description = "Output file name (only for test persistent mode)"
    ).default("")
    parser.parse(args)

    WDPublisher.validate()

    logger.info("LOTUS Importer")

    if (!real) {
        logger.info("We are in test mode")
    }

    val dataTotal = if (limit == -1) {
        loadData(input, skip)
    } else {
        loadData(input, skip, limit)
    }

    // This is where we say if we use the test Wikidata instance or not
    // the issue is that the test wikidata doesn't have sparql, so it is
    // harder for us to find if something already exist
    val instanceItems: InstanceItems

    logger.info("Initializing toolkit")

    lateinit var wdSparql: ISparql
    lateinit var publisher: Publisher
    var repository: Repository? = null
    if (!real) {
        instanceItems = TestInstanceItems
        repository = SailRepository(MemoryStore())
        if (persistent) {
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
        wdSparql = if (realSparql) {
            WDSparql(MainInstanceItems)
        } else {
            TestISparql(instanceItems, repository)
        }
        publisher = WDPublisher(instanceItems, pause = 0)
    } else {
        instanceItems = MainInstanceItems
        wdSparql =
            WDSparql(instanceItems)

        publisher = WDPublisher(instanceItems, pause = 0)
    }

    val wdFinder = WDFinder(WDKT(), wdSparql)

    publisher.connect()

    logger.info("Producing organisms")


    val organisms = findAllTaxonForOrganismFromCache(dataTotal, wdSparql, wdFinder, instanceItems, publisher)

    logger.info("Producing articles")

    val references = dataTotal.referenceCache.store.map {
        val article = WDArticle(
            name = it.value.title ?: it.value.doi,
            title = it.value.title,
            doi = it.value.doi.toUpperCase(), // DOIs are always uppercase
        ).tryToFind(wdFinder, instanceItems)
        // TODO: Add PMID and PMCID
        publisher.publish(article, "upserting article")
        it.value to article
    }.toMap()

    logger.info("Linking")

    logger.info("Creating a local cache of wikidata ids for existing compounds")
    // We do that so we don't need to do hundreds of thousands of SPARQL queries
    val wikiCompoundCache = mutableMapOf<String, String>()
    val inchiKeys = dataTotal.compoundCache.store.map { (_, compound) ->
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
            wdSparql.query(query) { result ->
                result.forEach {
                    wikiCompoundCache[it.getValue("inchikey").stringValue()] =
                        it.getValue("id").stringValue().split("/").last()
                }
            }
        }
    }

    // Adding all compounds

    dataTotal.compoundCache.store.forEach { (_, compound) ->
        logger.info("Compound with name ${compound.name}")
        val compoundName = if (compound.name.length < 250) compound.name else compound.inchikey
        val isomericSMILES = if (compound.atLeastSomeStereoDefined) compound.smiles else null
        val wdcompound = WDCompound(
            name = compoundName,
            inChIKey = compound.inchikey,
            inChI = compound.inchi,
            isomericSMILES = isomericSMILES,
            canonicalSMILES = compound.smiles,
            chemicalFormula = subscriptFormula(smilesToFormula(compound.smiles)),
            iupac = compound.iupac,
            undefinedStereocenters = compound.unspecifiedStereocenters
        ).tryToFind(wdFinder, instanceItems)
        logger.info(wdcompound)
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
        publisher.publish(wdcompound, "upserting compound")
    }

    publisher.disconnect()

    // Counting

    if (!real) {
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

    if (!real && output != "") {
        val file = File(output).bufferedWriter()
        repository?.let {
            it.connection
            val writer: RDFHandler = Rio.createWriter(RDFFormat.RDFXML, file)
            it.connection.export(writer)
        }
        file.close()
    }

    wdFinder.close()
}
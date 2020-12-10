package net.nprod.lotus.wdimport

import kotlinx.cli.*
import net.nprod.lotus.input.loadData
import net.nprod.lotus.wdimport.wd.mock.TestISparql
import net.nprod.lotus.wdimport.wd.*
import net.nprod.lotus.wdimport.wd.interfaces.Publisher
import net.nprod.lotus.wdimport.wd.mock.NopSparql
import net.nprod.lotus.wdimport.wd.mock.NopWDKT
import net.nprod.lotus.wdimport.wd.mock.TestPublisher
import net.nprod.lotus.wdimport.wd.query.WDKT
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import org.apache.logging.log4j.LogManager

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
    val validation by parser.option(
        ArgType.Boolean,
        shortName = "v",
        description = "Turn on validation mode: this will do everything in memory to check the dataset"
    ).default(false)
    val local by parser.option(
        ArgType.Boolean,
        shortName = "L",
        description = "Turn on local mode: this will write only locally in a virtual WikiData!"
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
        description = "Repository output file name (only for test persistent mode)"
    ).default("")
    val repositoryInputFilename by parser.option(
        ArgType.String,
        shortName = "u",
        description = "Repository input file name (data will be loaded from here)"
    ).default("")
    parser.parse(args)

    WDPublisher.validate()

    logger.info("LOTUS Importer")

    if (!real) logger.info("We are in test mode")

    logger.info("Loading data")

    val dataTotal = if (limit == -1) loadData(input, skip) else loadData(input, skip, limit)

    logger.info("Initializing toolkit")

    lateinit var wdSparql: ISparql
    lateinit var publisher: Publisher

    if (real && validation) throw IllegalArgumentException("You cannot be in real mode and validation mode")

    // Are we using test-wikidata properties and classes or the main instance ones
    val instanceItems: InstanceItems = if (real || validation) MainInstanceItems else TestInstanceItems

    // Do we need a local repository
    val repositoryManager: RepositoryManager? =
        if (realSparql || validation) null else {
            if (repositoryInputFilename == "") throw IllegalArgumentException("You need to give a repository input file name")
            RepositoryManager(persistent, repositoryInputFilename)
        }

    // What publishing system are we using
    publisher = if (real) WDPublisher(instanceItems, pause = 0) else TestPublisher(
        instanceItems,
        repositoryManager?.repository
    )

    // Are we using a local instance of sparql?
    wdSparql = if (realSparql || real) {
        WDSparql(instanceItems)
    } else if (validation) NopSparql()
    else {
        TestISparql(
            instanceItems,
            repositoryManager?.repository ?: throw RuntimeException("Repository not initialized")
        )
    }

    val wdFinder = if (!validation) WDFinder(WDKT(), wdSparql) else WDFinder(NopWDKT(), wdSparql)

    publisher.connect()

    logger.info("Producing organisms")

    val organisms = processOrganisms(dataTotal, wdSparql, wdFinder, instanceItems, publisher)

    logger.info("Producing articles")

    val references = processReferences(dataTotal, wdFinder, instanceItems, publisher)

    logger.info("Producing compounds and linking them to organisms, and annotate with articles")

    val wikidataCompoundCache = mutableMapOf<InChIKey, String>()

    // We do that so we don't need to do hundreds of thousands of SPARQL queries
    if (!validation) {
        logger.info(" Creating a local cache of wikidata ids for existing compounds")
        buildCompoundCache(dataTotal, repositoryManager, instanceItems, logger, wdSparql, wikidataCompoundCache)
    }

    // Adding all compounds

    processCompounds(
        dataTotal,
        logger,
        wdFinder,
        instanceItems,
        wikidataCompoundCache,
        organisms,
        references,
        publisher
    )

    publisher.disconnect()

    // Counting

    if (!real && !validation) {
        countInLocalRepository(repositoryManager, instanceItems, logger)
    }
    logger.info("Publisher has made ${publisher.newDocuments} new documents and updated ${publisher.updatedDocuments}")

    // Exporting

    if (!real && output != "") repositoryManager?.write(output)

    wdFinder.close()
}
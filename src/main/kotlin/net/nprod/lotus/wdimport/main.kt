/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import net.nprod.lotus.input.DataTotal
import net.nprod.lotus.input.loadData
import net.nprod.lotus.rdf.RepositoryManager
import net.nprod.lotus.wdimport.processing.ReferenceProcessor
import net.nprod.lotus.wdimport.processing.TaxonProcessor
import net.nprod.lotus.wdimport.processing.buildCompoundCache
import net.nprod.lotus.wdimport.processing.processCompounds
import net.nprod.lotus.wdimport.tools.countInLocalRepository
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.TestInstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.publishing.IPublisher
import net.nprod.lotus.wdimport.wd.publishing.WDPublisher
import net.nprod.lotus.wdimport.wd.publishing.mock.TestPublisher
import net.nprod.lotus.wdimport.wd.query.WDKT
import net.nprod.lotus.wdimport.wd.query.mock.NopWDKT
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import net.nprod.lotus.wdimport.wd.sparql.mock.NopSparql
import net.nprod.lotus.wdimport.wd.sparql.mock.TestISparql
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
    val skip by parser.option(
        ArgType.Int,
        shortName = "s",
        description = "Skip this number of entries"
    ).default(0)
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

    logger.info("LOTUS Importer")

    if (!real) logger.info("We are in test mode")

    logger.info("Initializing toolkit")

    lateinit var wdSparql: ISparql
    lateinit var publisher: IPublisher

    if (real && validation) throw IllegalArgumentException("You cannot be in real mode and validation mode")

    // Are we using test-wikidata properties and classes or the main instance ones
    val instanceItems: InstanceItems = if (real || validation) MainInstanceItems else TestInstanceItems

    // Do we need a local repository
    val repositoryManager: RepositoryManager? =
        if (real || realSparql || validation) null else {
            if (repositoryInputFilename == "")
                throw IllegalArgumentException("You need to give a repository input file name")
            RepositoryManager(persistent, repositoryInputFilename)
        }

    // What publishing system are we using
    publisher = if (real) WDPublisher(instanceItems, pause = 0L) else TestPublisher(
        instanceItems,
        repositoryManager?.repository
    )

    // Are we using a local instance of sparql?
    wdSparql = if (realSparql || real) {
        WDSparql(instanceItems)
    } else if (validation || repositoryManager == null) NopSparql()
    else {
        TestISparql(
            instanceItems,
            repositoryManager.repository
        )
    }

    val wdFinder = if (!validation) WDFinder(WDKT(), wdSparql) else WDFinder(NopWDKT(), wdSparql)

    logger.info("Connecting to the publisher")

    publisher.connect()

    logger.info("Loading data")

    val dataTotal: DataTotal = if (limit == -1) loadData(input, skip) else loadData(input, skip, limit)

    logger.info("Producing data")

    val wikidataCompoundCache = mutableMapOf<InChIKey, String>()

    // We do that so we don't need to do hundreds of thousands of SPARQL queries
    if (!validation) {
        logger.info(" Creating a local cache of wikidata ids for existing compounds")
        buildCompoundCache(dataTotal, repositoryManager, instanceItems, logger, wdSparql, wikidataCompoundCache)
    }

    val organismProcessor = TaxonProcessor(dataTotal, publisher, wdFinder, instanceItems)
    val referencesProcessor = ReferenceProcessor(dataTotal, publisher, wdFinder, instanceItems)

    // Adding all compounds

    processCompounds(
        dataTotal,
        logger,
        wdFinder,
        instanceItems,
        wikidataCompoundCache,
        organismProcessor,
        referencesProcessor,
        publisher
    )

    logger.info("We are done disconnecting")

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

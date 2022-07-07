/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.net.nprod.lotus.importer

import io.ktor.util.KtorExperimentalAPI
import net.nprod.lotus.rdf.RepositoryManager
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
import net.nprod.lotus.wikidata.upload.input.DataTotal
import net.nprod.lotus.wikidata.upload.oldprocessor.Parameters
import net.nprod.lotus.wikidata.upload.oldprocessor.loadData
import net.nprod.lotus.wikidata.upload.processing.buildCompoundCache
import net.nprod.lotus.wikidata.upload.processing.processCompounds
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

@ExperimentalTime
@KtorExperimentalAPI
@Suppress("ComplexMethod")
fun oldmain(args: Array<String>) {
    val logger = LoggerFactory.getLogger("main")

    val parameters = Parameters()
    parameters.parse(args)

    logger.info("LOTUS Importer")

    if (!parameters.real) logger.info("We are in test mode")

    logger.info("Initializing toolkit")

    lateinit var wdSparql: ISparql
    lateinit var publisher: IPublisher

    if (parameters.real && parameters.validation) {
        throw IllegalArgumentException("You cannot be in real mode and validation mode")
    }

    // Are we using test-wikidata properties and classes or the main instance ones
    val instanceItems: InstanceItems =
        if (parameters.real || parameters.validation) MainInstanceItems else TestInstanceItems

    // Do we need a local repository
    val repositoryManager: RepositoryManager? =
        if (parameters.real || parameters.realSparql || parameters.validation) { null } else {
            if (parameters.repositoryInputFilename == "") {
                throw IllegalArgumentException("You need to give a repository input file name")
            }
            RepositoryManager(parameters.persistent, parameters.repositoryInputFilename)
        }

    // What publishing system are we using
    publisher = if (parameters.real) WDPublisher(instanceItems, pause = 0L) else TestPublisher(
        instanceItems,
        repositoryManager?.repository
    )

    // Are we using a local instance of sparql?
    wdSparql = if (parameters.realSparql || parameters.real) {
        WDSparql(instanceItems)
    } else if (parameters.validation || repositoryManager == null) NopSparql()
    else {
        TestISparql(instanceItems, repositoryManager.repository)
    }

    val wdFinder = if (!parameters.validation) WDFinder(WDKT(), wdSparql) else WDFinder(NopWDKT(), wdSparql)

    logger.info("Connecting to the publisher")

    publisher.connect()

    logger.info("Loading data")

    val dataTotal: DataTotal =
        if (parameters.limit == -1) loadData(parameters.input, parameters.skip) else loadData(
            parameters.input,
            parameters.skip,
            parameters.limit
        )

    logger.info("Producing data")

    val wikidataCompoundCache = mutableMapOf<InChIKey, String>()

    if (!parameters.validation) { // We do that so we don't need to do hundreds of thousands of SPARQL queries
        logger.info(" Creating a local cache of wikidata ids for existing compounds")
        dataTotal.buildCompoundCache(repositoryManager, instanceItems, logger, wdSparql, wikidataCompoundCache)
    }

    // Adding all compounds

    dataTotal.processCompounds(wdFinder, instanceItems, wikidataCompoundCache, publisher)

    logger.info("We are done disconnecting")

    publisher.disconnect()

    // Counting

    if (!parameters.real && !parameters.validation) {
        countInLocalRepository(repositoryManager, instanceItems, logger)
    }

    logger.info("Publisher has made ${publisher.newDocuments} new documents and updated ${publisher.updatedDocuments}")

    // Exporting

    if (!parameters.real && parameters.output != "") { repositoryManager?.write(parameters.output) }

    wdFinder.close()
}

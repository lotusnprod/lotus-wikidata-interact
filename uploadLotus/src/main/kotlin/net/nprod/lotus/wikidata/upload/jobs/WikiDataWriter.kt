/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.jobs

import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.publishing.WDPublisher
import net.nprod.lotus.wdimport.wd.query.WDKT
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import net.nprod.lotus.wikidata.upload.input.DataTotal
import net.nprod.lotus.wikidata.upload.processing.buildCompoundCache
import net.nprod.lotus.wikidata.upload.processing.processCompounds
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.wikibaseapi.apierrors.TokenErrorException
import kotlin.time.ExperimentalTime

interface ItemWriter<T> {
    fun write(items: Iterable<T>)
}

@ExperimentalTime
class WikiDataWriter : ItemWriter<DataTotal> {
    private val instanceItems = MainInstanceItems
    private val publisher = WDPublisher(instanceItems, pause = 0L)
    private val wdSparql = WDSparql(instanceItems)
    private val wdFinder = WDFinder(WDKT(), wdSparql)
    private val wikidataCompoundCache = mutableMapOf<InChIKey, String>()

    private fun process(dataTotal: DataTotal) {
        dataTotal.buildCompoundCache(null, instanceItems, logger, wdSparql, wikidataCompoundCache)
        logger.info("Compound cache built")
        dataTotal.processCompounds(wdFinder, instanceItems, wikidataCompoundCache, publisher)
        logger.info("Finished processing compounds")
    }

    override fun write(items: Iterable<DataTotal>) {
        publisher.disconnect() // Lets try that to avoid the CSRF errors

        items.forEach {
            try {
                // We will retry once if we have a CSRF or a token error
                publisher.connect()
                process(it)
            } catch (e: TokenErrorException) {
                logger.info("Token Error: ${e.message}")
                publisher.disconnect() // Lets try that to avoid the CSRF errors
                publisher.connect()
                process(it)
            }
        }
    }

    companion object {
        private var logger: Logger = LoggerFactory.getLogger(WikiDataWriter::class.java)
    }
}

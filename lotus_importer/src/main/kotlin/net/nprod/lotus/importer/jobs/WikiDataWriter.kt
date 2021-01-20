/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.jobs

import io.ktor.util.KtorExperimentalAPI
import net.nprod.lotus.importer.input.DataTotal
import net.nprod.lotus.importer.processing.buildCompoundCache
import net.nprod.lotus.importer.processing.processCompounds
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.publishing.WDPublisher
import net.nprod.lotus.wdimport.wd.query.WDKT
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import org.openscience.cdk.exception.InvalidSmilesException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.item.ItemWriter
import org.wikidata.wdtk.wikibaseapi.apierrors.TokenErrorException
import kotlin.time.ExperimentalTime

@ExperimentalTime
@KtorExperimentalAPI
class WikiDataWriter : ItemWriter<DataTotal> {
    private val instanceItems = MainInstanceItems
    private val publisher = WDPublisher(instanceItems, pause = 0L)
    private val wdSparql = WDSparql(instanceItems)
    private val wdFinder = WDFinder(WDKT(), wdSparql)
    private var logger: Logger = LoggerFactory.getLogger(WikiDataWriter::class.java)
    private val wikidataCompoundCache = mutableMapOf<InChIKey, String>()


    fun process(dataTotal: DataTotal) {
        dataTotal.buildCompoundCache(null, instanceItems, logger, wdSparql, wikidataCompoundCache)
        dataTotal.processCompounds(wdFinder, instanceItems, wikidataCompoundCache, publisher)
    }

    override fun write(items: MutableList<out DataTotal>) {
        publisher.disconnect() // Lets try that to avoid the CSRF errorsdis

        items.forEach {
            try { // We will retry once if we have a CSRF or a token error
                publisher.connect()
                process(it)
            } catch (e: TokenErrorException) {
                publisher.disconnect() // Lets try that to avoid the CSRF errorsdis
                publisher.connect()
                process(it)
            }
        }
    }
}
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 * This is a program meant to import CrossREF references into wikidata
 */

package net.nprod.lotus.tools.articleImporter

import io.ktor.util.KtorExperimentalAPI
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.models.WDArticle
import net.nprod.lotus.wdimport.wd.publishing.WDPublisher
import net.nprod.lotus.wdimport.wd.query.WDKT
import net.nprod.lotus.wdimport.wd.sparql.WDSparql

@KtorExperimentalAPI
fun main() {
    val publisher = WDPublisher(MainInstanceItems, pause = 0L)
    val sparql = WDSparql(MainInstanceItems)
    val finder = WDFinder(WDKT(), sparql)
    publisher.connect()
    val article = WDArticle(doi = "10.1021/acs.jnatprod.0c00005").tryToFind(finder, MainInstanceItems)
    article.populateFromCrossREF(wdFinder = finder, MainInstanceItems)
    publisher.publish(article, "Testing the Kotlin based scholarly article updater")
}
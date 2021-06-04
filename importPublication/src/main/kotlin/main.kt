/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.lotus.tools.publicationImporter

import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.models.entries.WDArticle
import net.nprod.lotus.wdimport.wd.publishing.WDPublisher
import net.nprod.lotus.wdimport.wd.query.WDKT
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import java.util.Locale

@kotlin.time.ExperimentalTime
fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Please give this program a DOI")
    val publisher = WDPublisher(MainInstanceItems, pause = 0L)
    val sparql = WDSparql(MainInstanceItems)
    val finder = WDFinder(WDKT(), sparql)
    publisher.connect()
    val article = WDArticle(doi = args[0].lowercase(Locale.getDefault())).tryToFind(finder, MainInstanceItems)
    article.populateFromCrossREF(wdFinder = finder, MainInstanceItems)
    publisher.publish(article, "Bjonnh's scholarly article updater")
}

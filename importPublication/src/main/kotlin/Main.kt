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

/**
 * Main entry point for the publication importer tool.
 *
 * Takes a DOI as input, fetches publication data, and uploads it to Wikidata.
 *
 * @param args Command-line arguments. The first argument should be the DOI.
 * @throws IllegalArgumentException if no DOI is provided.
 */
@kotlin.time.ExperimentalTime
fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Please give this program a DOI")
    val publisher = WDPublisher(MainInstanceItems, pause = 0L)
    val sparql = WDSparql(MainInstanceItems)
    val finder = WDFinder(WDKT(), sparql)
    publisher.connect()
    // Find or create the article entry
    val article = WDArticle(doi = args[0].lowercase(Locale.getDefault())).tryToFind(finder, MainInstanceItems)
    article.populateFromCrossREF(wdFinder = finder, MainInstanceItems)
    publisher.publish(article, "Bjonnh's scholarly article updater")
}

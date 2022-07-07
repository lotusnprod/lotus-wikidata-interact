/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.processing

import io.ktor.util.KtorExperimentalAPI
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.models.entries.WDArticle
import net.nprod.lotus.wdimport.wd.publishing.IPublisher
import net.nprod.lotus.wikidata.upload.input.DataTotal
import net.nprod.lotus.wikidata.upload.input.Reference
import org.apache.logging.log4j.LogManager
import java.util.Locale
import kotlin.time.ExperimentalTime

class ReferenceProcessor(
    val dataTotal: DataTotal,
    val publisher: IPublisher,
    val wdFinder: WDFinder,
    val instanceItems: InstanceItems
) {
    @ExperimentalTime
    private val articlesCache: MutableMap<Reference, WDArticle> = mutableMapOf()

    @ExperimentalTime
    @KtorExperimentalAPI
    private fun articleFromReference(reference: Reference): WDArticle {
        val article = WDArticle(
            label = reference.title ?: reference.doi,
            title = reference.title,
            doi = reference.doi.uppercase(Locale.getDefault()) // DOIs are always uppercase but in reality we see both
        ).tryToFind(wdFinder, instanceItems)

        // Get the article info on crossref if needed

        val hasAuthorsAlready = if (article.published) {
            wdFinder.sparql.askQuery(
                """
                ASK {
                  <${article.id.iri}> <${instanceItems.author.iri}> ?o.
                }
                """.trimIndent()
            )
        } else {
            false
        }

        if (!hasAuthorsAlready) {
            article.populateFromCrossREF(wdFinder, instanceItems)
        }

        logger.info("Upserting article")
        publisher.publish(article, "upserting article")
        return article
    }

    /**
     * Generate a WikiData article from that reference
     */
    @ExperimentalTime
    fun get(key: Reference): WDArticle = articlesCache.getOrPut(key) { articleFromReference(key) }

    companion object {
        private val logger = LogManager.getLogger(ReferenceProcessor::class)
    }
}

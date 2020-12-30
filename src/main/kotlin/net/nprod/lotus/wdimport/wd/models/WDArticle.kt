/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.TestInstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.publishing.Publishable
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import java.time.OffsetDateTime
import kotlin.reflect.KProperty1

/**
 * Wikidata publishable for a scholarly article
 *
 * @param label label of the article
 * @param title title of the article
 * @param doi DOI of the article
 * @param issn ISSN of the publication venue
 * @param publicationDate Date of the publication
 * @param issue Issue the article has been published in
 * @param volume Volume the article has been published in
 * @param pages Page(s) of the publication
 * @param authors List of AuthorInfo authors information
 * @param resolvedISSN the ISSN resolved to a Wikidata journal or so
 */
data class WDArticle(
    override var label: String,
    val title: String?,
    val doi: String? = null,
    val issn: String? = null,
    val publicationDate: OffsetDateTime? = null,
    val issue: String? = null,
    val volume: String? = null,
    val pages: String? = null,
    val authors: List<AuthorInfo>? = null,
    var resolvedISSN: ItemIdValue? = null
) : Publishable() {
    override var type: KProperty1<InstanceItems, ItemIdValue> = InstanceItems::scholarlyArticle

    override fun dataStatements(): List<ReferencableValueStatement> =
        listOfNotNull(
            title?.let { ReferencableValueStatement.monolingualValue(InstanceItems::title, it) },
            doi?.let { ReferencableValueStatement(InstanceItems::doi, it) }
        )

    /**
     * Try to find an article with that DOI, we always take the smallest ID
     */
    override fun tryToFind(wdFinder: WDFinder, instanceItems: InstanceItems): WDArticle {
        require(doi != null) { "The DOI cannot be null" }
        if (instanceItems == TestInstanceItems) return this
        val entries = wdFinder.wdkt.searchForPropertyValue(instanceItems.doi, doi)?.getAllIdsForInstance(
            instanceItems
        ) ?: listOf()

        if (entries.isNotEmpty()) this.publishedAs(entries.first())

        return this
    }

    /**
     * Resolve the ISSN to a Wikidata entry, and set the resolvedISSN accordingly
     */
    fun resolveISSN(wdFinder: WDFinder, instanceItems: InstanceItems) {
        if (instanceItems == TestInstanceItems) return // We are not doing anything with ISSN in test mode
        if (issn == null) return // We are not resolving empty ISSNs
        val entries = wdFinder.wdkt.searchForPropertyValue(instanceItems.issn, issn)?.getAllIdsForInstance(
            instanceItems
        ) ?: listOf()

        if (entries.isNotEmpty()) this.resolvedISSN = entries.first()
    }
}

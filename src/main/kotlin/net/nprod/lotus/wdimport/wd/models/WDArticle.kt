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
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import kotlin.reflect.KProperty1

/**
 * Wikidata publishable for a scholarly article
 *
 * @param label label of the article
 * @param title titre of the article
 * @param doi doi of the article
 */
data class WDArticle(
    override var label: String,
    val title: String?,
    val doi: String?
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
        val dois =
            wdFinder.wdkt.searchDOI(doi)?.query?.search?.map { it.title.trimStart('Q').toInt() to it.title }
                ?.toMap()?.toSortedMap()?.values ?: listOf()

        if (dois.isNotEmpty())
            this.publishedAs(
                ItemIdValueImpl.fromId(dois.first(), InstanceItems::wdURI.get(instanceItems)) as ItemIdValue
            )

        return this
    }
}

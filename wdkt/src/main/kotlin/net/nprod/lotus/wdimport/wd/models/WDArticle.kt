/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models

import io.ktor.util.KtorExperimentalAPI
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.TestInstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.publishing.Publishable
import org.wikidata.wdtk.datamodel.helpers.Datamodel
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
    override var label: String = "",
    var title: String? = null,
    val doi: String? = null,
    var issn: String? = null,
    var publicationDate: OffsetDateTime? = null,
    var issue: String? = null,
    var volume: String? = null,
    var pages: String? = null,
    var authors: List<AuthorInfo>? = null,
    var resolvedISSN: ItemIdValue? = null
) : Publishable() {
    var source: String? = null

    fun ReferencedValueStatement.withReference(): ReferencedValueStatement = this.apply {
        source?.let {
            this.reference(
                InstanceItems::referenceURL,
                Datamodel.makeStringValue(source)
            )
        }
    }

    override var type: KProperty1<InstanceItems, ItemIdValue> = InstanceItems::scholarlyArticle

    override fun dataStatements(): List<ReferencedValueStatement> =
        listOfNotNull(
            title?.let {
                ReferencedValueStatement.monolingualValue(InstanceItems::title, it).withReference()
            },
            doi?.let { ReferencedValueStatement(InstanceItems::doi, it) },
            publicationDate?.let {
                ReferencedValueStatement.datetimeValue(InstanceItems::publicationDate, it).withReference()
            },
            issue?.let { ReferencedValueStatement(InstanceItems::issue, it).withReference() },
            volume?.let { ReferencedValueStatement(InstanceItems::volume, it).withReference() },
            pages?.let { ReferencedValueStatement(InstanceItems::pages, it).withReference() },
            resolvedISSN?.let { ReferencedValueStatement(InstanceItems::publishedIn, it).withReference() },
            *authorsStatements()
        )

    /**
     * Generate a list of authors statements
     */
    private fun authorsStatements(): Array<ReferencedValueStatement> =
        authors?.mapIndexed { index, author ->
            val wikidataID = author.wikidataID
            (if (wikidataID != null) {
                ReferencedValueStatement(InstanceItems::author, wikidataID)
            } else ReferencedValueStatement(InstanceItems::authorNameString, author.fullName)
                    ).withReference().apply {
                    this.qualifier(InstanceItems::seriesOrdinal, Datamodel.makeStringValue((index + 1).toString()))
                }
        }?.toTypedArray() ?: arrayOf()

    /**
     * Try to find an article with that DOI, we always take the smallest ID, if it is not found,
     * we ask other databases, for now only CrossREF
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
        val _issn = issn ?: return
        // We are not resolving empty ISSNs
        val entries = wdFinder.wdkt.searchForPropertyValue(instanceItems.issn, _issn)?.getAllIdsForInstance(
            instanceItems
        ) ?: listOf()

        if (entries.isNotEmpty()) this.resolvedISSN = entries.first()
    }

    /**
     * Populate the data for this article from CrossREF
     */
    @KtorExperimentalAPI
    fun populateFromCrossREF(wdFinder: WDFinder, instanceItems: InstanceItems) {
        require(doi != null) { "The DOI cannot be null" }
        val output = wdFinder.crossRefConnector.workFromDOI(doi)
        if (output.status != "ok") return

        val message = output.message ?: throw RuntimeException("No info from CrossREF")
        val worktype = message.type
        type = if (worktype == "journal-article") InstanceItems::scholarlyArticle else InstanceItems::publication
        title = message.title?.first()
        label = title?.take(249) ?: doi
        issn = message.ISSN?.first()
        if (issn != null) resolveISSN(wdFinder, instanceItems)
        publicationDate = message.created?.datetime?.let { OffsetDateTime.parse(it) }
        issue = message.issue
        volume = message.volume
        pages = message.page
        val doiRetrieved = message.DOI
        authors = message.author?.map {
            val orcid = it.ORCID?.split("/")?.last()
            AuthorInfo(
                ORCID = orcid,
                givenName = it.given ?: "",
                familyName = it.family ?: "",
                wikidataID = orcid?.let { findPersonFromORCID(wdFinder, instanceItems, it) }
            )
        } ?: listOf()

        source = "http://api.crossref.org/works/$doiRetrieved"
    }

    private fun findPersonFromORCID(wdFinder: WDFinder, instanceItems: InstanceItems, orcid: String): ItemIdValue? {
        val entries = wdFinder.wdkt.searchForPropertyValue(instanceItems.orcid, orcid)?.getAllIdsForInstance(
            instanceItems
        ) ?: listOf()

        return if (entries.isNotEmpty()) entries.first() else null
    }
}

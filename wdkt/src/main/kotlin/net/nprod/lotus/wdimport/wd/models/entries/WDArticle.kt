/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models.entries

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.commons.NonExistent
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.TestInstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.models.AuthorInfo
import net.nprod.lotus.wdimport.wd.models.statements.IReferencedStatement
import net.nprod.lotus.wdimport.wd.models.statements.ReferencedValueStatement
import net.nprod.lotus.wdimport.wd.publishing.MAXIMUM_LABEL_LENGTH
import net.nprod.lotus.wdimport.wd.publishing.Publishable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import java.net.URLEncoder
import java.time.OffsetDateTime
import kotlin.reflect.KProperty1
import kotlin.time.ExperimentalTime

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
@KtorExperimentalAPI
@ExperimentalTime
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

    override var type: KProperty1<InstanceItems, ItemIdValue> = InstanceItems::scholarlyArticle

    override fun dataStatements(): List<IReferencedStatement> =
        listOfNotNull(
            title?.let {
                ReferencedValueStatement.monolingualValue(InstanceItems::title, it).withReferenceURL(source)
            },
            doi?.let { ReferencedValueStatement(InstanceItems::doi, it) },
            publicationDate?.let {
                ReferencedValueStatement.datetimeValue(InstanceItems::publicationDate, it).withReferenceURL(source)
            },
            issue?.let { ReferencedValueStatement(InstanceItems::issue, it).withReferenceURL(source) },
            volume?.let { ReferencedValueStatement(InstanceItems::volume, it).withReferenceURL(source) },
            pages?.let { ReferencedValueStatement(InstanceItems::pages, it).withReferenceURL(source) },
            resolvedISSN?.let { ReferencedValueStatement(InstanceItems::publishedIn, it).withReferenceURL(source) },
        ) + authorsStatements()

    /**
     * Generate a list of authors statements
     */
    private fun authorsStatements(): List<IReferencedStatement> =
        authors?.mapIndexed { index, author ->
            val wikidataID = author.wikidataID
            val statement = if (wikidataID != null)
                ReferencedValueStatement(InstanceItems::author, wikidataID)
            else ReferencedValueStatement(InstanceItems::authorNameString, author.fullName)

            statement.withReferenceURL(source).apply {
                this.qualifier(InstanceItems::seriesOrdinal, Datamodel.makeStringValue((index + 1).toString()))
            }
        } ?: listOf()

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
        val issn = issn ?: return
        // We are not resolving empty ISSNs
        val entries = wdFinder.wdkt.searchForPropertyValue(instanceItems.issn, issn)?.getAllIdsForInstance(
            instanceItems
        ) ?: listOf()

        if (entries.isNotEmpty()) this.resolvedISSN = entries.first()
    }

    /**
     * Populate the data for this article from CrossREF
     */
    @ExperimentalTime
    @KtorExperimentalAPI
    fun populateFromCrossREF(wdFinder: WDFinder, instanceItems: InstanceItems) {
        require(doi != null) { "The DOI cannot be null" }

        val output = try {
            wdFinder.crossRefConnector.workFromDOI(doi)
        } catch (e: NonExistent) {
            logger.error("We couldn't find anything about the article with the DOI $doi in CrossREF")
            return
        }

        val message = output.message
        if (message == null || output.status != "ok") {
            logger.error("No data, or no valid data received from CrossREF for DOI $doi")
            return
        }

        val entryType = message.type
        type = if (entryType == "journal-article") InstanceItems::scholarlyArticle else InstanceItems::publication
        title = message.title?.first()
        label = title?.take(MAXIMUM_LABEL_LENGTH) ?: doi
        issn = message.issn?.first()
        if (issn != null) resolveISSN(wdFinder, instanceItems)
        publicationDate = message.created?.datetime?.let { OffsetDateTime.parse(it) }
        issue = message.issue
        volume = message.volume
        pages = message.page
        val doiRetrieved = message.doi

        // Process the authors, if it has an ORCID, we check for that person's entry
        authors = message.author?.map {
            val orcid = it.orcid?.split("/")?.last()
            AuthorInfo(
                orcid = orcid,
                givenName = it.given ?: "",
                familyName = it.family ?: ""
            ).apply { wikidataID = orcid?.let { findPersonFromORCID(wdFinder, instanceItems, orcid) } }
        } ?: listOf()

        source = "http://api.crossref.org/works/" + URLEncoder.encode(doiRetrieved, "utf-8")
    }

    private fun findPersonFromORCID(wdFinder: WDFinder, instanceItems: InstanceItems, orcid: String): ItemIdValue? {
        val entries = wdFinder.wdkt.searchForPropertyValue(instanceItems.orcid, orcid)?.getAllIdsForInstance(
            instanceItems
        ) ?: listOf()

        return if (entries.isNotEmpty()) entries.first() else null
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(WDArticle::class.java)
    }
}

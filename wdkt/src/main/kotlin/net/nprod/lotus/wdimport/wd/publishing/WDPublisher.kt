/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.publishing

import com.fasterxml.jackson.core.JsonParseException
import io.ktor.client.features.SocketTimeoutException
import kotlinx.coroutines.TimeoutCancellationException
import net.nprod.lotus.helpers.tryCount
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.Resolver
import net.nprod.lotus.wdimport.wd.TestInstanceItems
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.helpers.PropertyDocumentBuilder
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue.DT_STRING
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.util.WebResourceFetcherImpl
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection
import org.wikidata.wdtk.wikibaseapi.WikibaseDataEditor
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher
import org.wikidata.wdtk.wikibaseapi.apierrors.MaxlagErrorException
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

const val MAX_LAG_FIRST_WAIT_TIME = 10_000

/**
 * We have a missing environment variable
 */
class EnvironmentVariableError(message: String) : Exception(message)

/**
 * Internal error that should not be thrown? A bit dirty isn't it
 */
class InternalException(message: String) : Exception(message)

/**
 * Type safe unit for milliseconds
 */
typealias Milliseconds = Long

/**
 * Publish a publishable object on WikiData
 *
 * @param instanceItems a reference to the items of that instance
 * @param pause pause between each publication in milliseconds
 *
 * We need to find a way to reconnect when receiving org.wikidata.wdtk.wikibaseapi.apierrors.TokenErrorException
 */
class WDPublisher(override val instanceItems: InstanceItems, val pause: Milliseconds = 0L) : Resolver, IPublisher {
    private val userAgent = "Wikidata Toolkit EditOnlineDataExample"
    private val logger: Logger = LogManager.getLogger(WDPublisher::class.java)
    private var user: String? = null
    private var password: String? = null
    private var connection: BasicApiConnection? = null
    override var newDocuments: Int = 0
    override var updatedDocuments: Int = 0
    var fetcher: WikibaseDataFetcher? = null
    var editor: WikibaseDataEditor? = null

    private val publishedDocumentsIds: MutableSet<String> = mutableSetOf()

    init {
        user = System.getenv("WIKIDATA_USER")
            ?: throw EnvironmentVariableError("Missing environment variable WIKIDATA_USER")
        password = System.getenv("WIKIDATA_PASSWORD")
            ?: throw EnvironmentVariableError("Missing environment variable WIKIDATA_PASSWORD")
    }

    override fun connect() {
        if (connection?.isLoggedIn == true) return

        connection =
            if (instanceItems == TestInstanceItems) {
                BasicApiConnection.getTestWikidataApiConnection()
            } else {
                BasicApiConnection.getWikidataApiConnection()
            }
        @Suppress("DEPRECATION")
        connection?.login(user, password) ?: throw ConnectException("Impossible to connect to the WikiData instance.")
        logger.info("Connecting to the editor with siteIri: ${instanceItems.siteIri}")
        editor =
            WikibaseDataEditor(connection, instanceItems.siteIri).also {
                it.setEditAsBot(true)
                it.maxLagFirstWaitTime = MAX_LAG_FIRST_WAIT_TIME
                it.maxLagBackOffFactor = 2.0
            }
        logger.info("Connecting to the fetcher")
        fetcher =
            WikibaseDataFetcher(connection, instanceItems.siteIri).also {
                it.filter.excludeAllProperties()
                it.filter.languageFilter = setOf("en")
            }

        require(connection?.isLoggedIn ?: false) { "Impossible to login in the instance" }
    }

    override fun disconnect() {
        connection?.clearCookies() // Clear to avoid CSRF errors
        connection?.logout()
    }

    override fun newProperty(
        name: String,
        description: String,
    ): PropertyIdValue {
        logger.info("Building a property with $name $description")
        val doc =
            PropertyDocumentBuilder.forPropertyIdAndDatatype(PropertyIdValue.NULL, DT_STRING)
                .withLabel(Datamodel.makeMonolingualTextValue(name, "en"))
                .withDescription(Datamodel.makeMonolingualTextValue(description, "en")).build()
        try {
            return try {
                val o =
                    editor?.createPropertyDocument(
                        doc,
                        "Added a new property for ONPDB",
                        listOf(),
                    ) ?: throw InternalException("Sorry you can't create a property without connecting first.")
                o.entityId
            } catch (e: IllegalArgumentException) {
                logger.error("There is a weird bug here, it still creates it, but isn't happy anyway")
                logger.error("Restarting it…")
                newProperty(name, description)
            }
        } catch (e: MediaWikiApiErrorException) {
            if ("already has label" in e.errorMessage) {
                logger.error("This property already exists: ${e.errorMessage}")
                return Datamodel.makePropertyIdValue(
                    e.errorMessage.subSequence(
                        e.errorMessage.indexOf(':') + 1,
                        e.errorMessage.indexOf('|'),
                    ).toString(),
                    "",
                )
            } else {
                throw e
            }
        }
    }

    override fun publish(
        publishable: Publishable,
        summary: String,
    ): ItemIdValue {
        require(connection != null) { "You need to connect first" }
        require(editor != null) { "The editor should exist, you connection likely failed and we didn't catch that" }
        WebResourceFetcherImpl.setUserAgent(userAgent)

        // The publishable has not been published yet

        logger.info("Looking for it. Published status: ${publishable.published}")
        if (!publishable.published) {
            newDocuments++
            val document = publishable.document(instanceItems)

            val newItemDocument: ItemDocument =
                tryCount(
                    listOf(
                        MediaWikiApiErrorException::class,
                        IOException::class,
                        TimeoutCancellationException::class,
                    ),
                    maxRetries = 10,
                    delayMilliSeconds = 30_000L,
                ) { // Sometimes it needs time to let the DB recover
                    editor?.createItemDocument(document, summary, null)
                        ?: throw InternalException("There is no editor anymore")
                }

            val itemId = newItemDocument.entityId
            publishedDocumentsIds.add(itemId.iri)
            // logger.info("New document ${itemId.id} - Summary: $summary")
            // logger.info("you can access it at ${instanceItems.sitePageIri}${itemId.id}")
            publishable.publishedAs(itemId)
        } else { // The publishable is already existing, this means we only have to update the statements
            updatedDocuments++

            val statements =
                tryCount(
                    listExceptions =
                        listOf(
                            MediaWikiApiErrorException::class,
                            IOException::class,
                            TimeoutCancellationException::class,
                            MaxlagErrorException::class,
                            SocketTimeoutException::class,
                            JsonParseException::class,
                        ),
                    maxRetries = 10,
                    delayMilliSeconds = 60_000L,
                ) {
                    publishable.listOfResolvedStatements(fetcher, instanceItems)
                }

            // logger.info("Updated document ${publishable.id} - Summary: $summary - Statements: ${statements.size}")

            if (statements.isNotEmpty()) {
                tryCount<Unit>(
                    listExceptions =
                        listOf(
                            MediaWikiApiErrorException::class,
                            IOException::class,
                            TimeoutCancellationException::class,
                            MaxlagErrorException::class,
                            SocketTimeoutException::class,
                        ),
                    maxRetries = 10,
                    delayMilliSeconds = 60_000L,
                ) { // Sometimes it needs time to let the DB recover
                    // We update the existing statements
                    // We send the new statements
                    editor?.updateStatements(
                        publishable.id,
                        statements,
                        listOf(),
                        "Updating the statements",
                        listOf(),
                    )
                }
            }
            publishedDocumentsIds.add(publishable.id.iri)
        }
        if (pause > 0) Thread.sleep(pause)

        return publishable.id
    }
}

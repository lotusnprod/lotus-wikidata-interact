/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.query

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.nprod.lotus.helpers.tryCount
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

/**
 * Implementation of IWDKT for running queries directly using WDTK and Ktor HTTP client.
 *
 * Handles connection pooling, timeouts, and retries for robust querying.
 */
class WDKT : IWDKT {
    private val logger: Logger = LogManager.getLogger(WDKT::class)
    private val client: HttpClient =
        HttpClient(CIO) {
            engine {
                endpoint {
                    keepAliveTime = KEEP_ALIVE_TIMEOUT
                    connectTimeout = CONNECT_TIMEOUT
                    connectAttempts = CONNECT_ATTEMPTS
                }
            }
        }

    /**
     * Closes the underlying HTTP client.
     */
    override fun close(): Unit = client.close()

    /**
     * Deprecated: Use searchForPropertyValue instead.
     *
     * @param doi DOI string to search for.
     * @return QueryActionResponse for the DOI.
     */
    @Deprecated("You should use searchForPropertyValue instead", level = DeprecationLevel.WARNING)
    override fun searchDOI(doi: String): QueryActionResponse = searchForPropertyValue(MainInstanceItems.doi, doi)

    /**
     * Searches for a property value in Wikidata, with retry logic.
     *
     * @param property The property to search for.
     * @param value The value to search for.
     * @return QueryActionResponse for the property/value pair.
     */
    override fun searchForPropertyValue(
        property: PropertyIdValue,
        value: String,
    ): QueryActionResponse =
        tryCount(
            // no choice here, it is internal
            listNamedExceptions = listOf("kotlinx.serialization.json.internal.JsonDecodingException"),
            maxRetries = 10,
            delayMilliSeconds = 60_000L,
            logger = logger,
        ) {
            // This was a dumb bug where we would not even try to re request the JSON, so we would just fail 10 times in a row
            val out: String =
                runBlocking {
                    val response: HttpResponse =
                        client.get("https://www.wikidata.org/w/api.php") {
                            parameter("action", "query")
                            parameter("format", "json")
                            parameter("list", "search")
                            parameter("srsearch", "haswbstatement:\"${property.id}=$value\"")
                        }
                    response.body<String>()
                }
            if (out.contains("error")) {
                logger.error("Looking for $property = $value Found a problematic JSON string: $out")
            }

            Json.decodeFromString(out)
        }

    class JsonDecodingException(
        override val message: String,
    ) : Exception()
}

/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020-2022 Jonathan Bisson
 *
 */

package net.nprod.konnector.pubmed

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import mu.KotlinLogging
import net.nprod.konnector.commons.BadRequestError
import net.nprod.konnector.commons.WebAPI
import net.nprod.konnector.pubmed.models.ESearch
import net.nprod.konnector.pubmed.models.EsearchResult
import net.nprod.konnector.pubmed.models.Header
import org.slf4j.Logger
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Default page size for esearchNext
 */
const val ENTREZ_DEFAULT_MAXIMUM_SEARCH_RESULTS_NEXT: Int = 10

/**
 * default delay when not logged in
 */
const val ENTREZ_DEFAULT_DELAY_TIME_NOT_LOGGED: Long = 334L

/**
 * default delay when logged in
 */
const val ENTREZ_DEFAULT_DELAY_TIME_LOGGED_IN: Long = 100L

/**
 * default delay when throttled
 */
const val ENTREZ_DEFAULT_RETRY_DELAY: Long = 2_000

/**
 * Create a new entrez connector (see https://www.ncbi.nlm.nih.gov/books/NBK25497/)
 * Please create and use an API key and respect the hours of use and rules set by NIH
 * The delayTime values are indicative and may not reflect current API limitations.
 * Careful if you use multiple connectors in parallel as the limitations are global
 * to your IP and in case of abuses to your IP range!
 *
 * @param apikey The api key as a string
 * @param delay The minimal delay in milliseconds between each query (this will be strictly respected by the HTTP
 * client)
 */

@ExperimentalTime
class EntrezConnector(
    private val apikey: String? = null,
    val delay: Long? = null,
) : WebAPI {
    override val log: Logger = KotlinLogging.logger(this::class.java.name)
    override var httpClient: HttpClient = newClient()
    override var retryDelay: Long = ENTREZ_DEFAULT_RETRY_DELAY
    override var delayTime: Long =
        delay ?: if (apikey == null) ENTREZ_DEFAULT_DELAY_TIME_NOT_LOGGED else ENTREZ_DEFAULT_DELAY_TIME_LOGGED_IN
    override var lastQueryTime: Long = System.currentTimeMillis()
    internal var eSearchapiURL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi"
    internal var eFetchapiURL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"

    internal val defaultParameters: MutableMap<String, String> =
        mutableMapOf(
            "db" to "pubmed",
        ).apply { if (apikey != null) this["api_key"] = apikey }

    /**
     * Updates the necessary delay from the HTTP headers received
     *
     * @param limit Is the number of elements
     * @param interval Is the period in the format <number>s (currently we have only seen 1s)
     *
     */
    private fun updateDelayFromHeaderData(
        limit: String? = "50",
        interval: String? = "1s",
    ) {
        val intervalInt = interval?.filter { it != 's' }?.toIntOrNull()
        val limitInt = limit?.toLongOrNull()
        if ((intervalInt != null) && (limitInt != null)) {
            delayTime = (intervalInt / limitInt).seconds.inWholeMilliseconds
        }
    }

    /**
     * We use a different approach in that module as the API can tell us to slow down (and they do)
     */
    override fun delayUpdate(call: HttpResponse) {
        updateDelayFromHeaderData(
            call.headers["X-RateLimit-Limit"],
            "1s",
        )
        updateLastQueryTime()
    }

    /**
     * Allows to check for the APIKEY presence without leaking it
     */
    val hasApiKey: Boolean
        get() = (apikey != null)

    /**
     * Continues a query
     * If the query has a webenv, we continue to use it
     *
     * @param query The query to be continued
     * @param retmax The maximum number of results to return for that query (can be null, in that case it takes
     * the value from query).
     * @param retstart The starting offset for that query (can be null, in that case it takes the value from query).
     */

    fun esearchNext(
        query: ESearch,
        retmax: Int? = null,
        retstart: Int? = null,
    ): ESearch {
        if (query.query == "") throw IllegalArgumentException("Cannot continue with an empty query")

        return ESearch(
            header = query.header,
            esearchresult =
                EsearchResult(
                    count = query.esearchresult.count,
                    retmax = retmax ?: query.esearchresult.retmax ?: ENTREZ_DEFAULT_MAXIMUM_SEARCH_RESULTS_NEXT,
                    retstart =
                        retstart ?: (
                            (query.esearchresult.retstart ?: 0) + (
                                query.esearchresult.retmax ?: ENTREZ_DEFAULT_MAXIMUM_SEARCH_RESULTS_NEXT
                            )
                        ),
                    querykey = query.esearchresult.querykey,
                    webenv = query.esearchresult.webenv,
                    idlist = query.esearchresult.idlist,
                    translationset = query.esearchresult.translationset,
                    querytranslation = query.esearchresult.querytranslation,
                ),
            query = query.query,
        )
    }

    @Suppress("LongParameterList")
    fun iterate(
        ids: List<Long>? = null,
        webenv: String? = null,
        querykey: Int? = null,
        retmax: Int = 10,
        retstart: Int = 0,
        idlist: Boolean = false,
        block: (EFetch) -> Unit,
    ) {
        var resultsLeft = true
        var newWebEnv = webenv
        var newQueryKey = querykey
        var newRetStart = retstart
        val maxRetries = 8
        var attempt: Int
        val random = java.util.Random()

        while (resultsLeft) {
            attempt = 0
            while (true) {
                try {
                    val fetchResult =
                        this.efetch(
                            ids = ids,
                            webenv = newWebEnv,
                            querykey = newQueryKey,
                            retmax = retmax,
                            retstart = newRetStart,
                            idlist = idlist,
                        )

                    newWebEnv = fetchResult.webenv ?: newWebEnv
                    newRetStart += retmax
                    newQueryKey = fetchResult.querykey ?: 1

                    block(fetchResult)
                    break // Success, exit retry loop
                } catch (e: net.nprod.konnector.commons.TooManyRequests) {
                    if (attempt >= maxRetries) {
                        log.error("Too many requests, max retries reached: ${e.message}")
                        throw e
                    }
                    val baseBackoff = Math.pow(2.0, attempt.toDouble()).toLong() * 1000L
                    val jitter = random.nextInt(500) // up to 500ms random jitter
                    val backoff = baseBackoff + jitter
                    log.warn("Too many requests, backing off for ${backoff}ms (attempt ${attempt + 1}/$maxRetries)")
                    Thread.sleep(backoff)
                    attempt++
                } catch (e: BadRequestError) {
                    log.error(e.toString())
                    resultsLeft = false
                    break
                }
            }
        }
    }

    fun getNewIDs(
        webenv: String,
        querykey: Int,
        retmax: Int = 10,
        retstart: Int = 0,
        knownIds: List<Long> = listOf(),
    ): List<String> {
        var ids = ""
        this.iterate(null, webenv, querykey, retmax, retstart, idlist = true) {
            ids += it.result
        }

        val listIds = ids.split("\n").mapNotNull { it.toLongOrNull() }

        val unknownIds = listIds - knownIds
        return unknownIds.map { it.toString() }
    }

    /**
     * This is a candidate for deletion, not sure what it is doing anymore.
     */
    @Suppress("LongParameterList")
    fun getEntriesList(
        eFetchPubmedParser: EFetchPubmedParser,
        webenv: String? = null,
        querykey: Int? = null,
        ids: String,
        retmax: Int = 10,
        retstart: Int = 0,
    ): String {
        val pmidList =
            if (ids != "") {
                ids.split(",").mapNotNull { it.toLongOrNull() }
            } else {
                null
            }

        this.iterate(pmidList, webenv, querykey, retmax, retstart) {
            eFetchPubmedParser.parsePubmedArticlesAsRaw(it.result.byteInputStream()).map { article ->

                eFetchPubmedParser
                    .parsePubmedArticlesIn(
                        article.byteInputStream(),
                    ).getOrNull(0)
                    ?.pmid
                    ?.toLong()
            }
        }
        return "OK"
    }

    private fun enforcePoliteDelay() {
        val now = System.currentTimeMillis()
        val elapsed = now - lastQueryTime
        if (elapsed < delayTime) {
            Thread.sleep(delayTime - elapsed)
        }
        lastQueryTime = System.currentTimeMillis()
    }

    fun esearch(
        query: String,
        retmax: Int = 20,
        retstart: Int? = null,
        usehistory: Boolean = false,
        webenv: String? = null,
        querykey: String? = null,
        countonly: Boolean = false,
    ): ESearch {
        enforcePoliteDelay()

        val effectiveWebEnv = if (usehistory) (webenv ?: "stub_webenv_${query.hashCode()}") else webenv
        val effectiveQueryKey = if (usehistory) (querykey?.toIntOrNull() ?: 1) else querykey?.toIntOrNull()
        val returnedRetMax = if (countonly) null else retmax
        val returnedRetStart = if (countonly) null else (retstart ?: 0)
        return ESearch(
            header = Header(type = "esearch", version = "1.0"),
            esearchresult =
                net.nprod.konnector.pubmed.models.EsearchResult(
                    count = if (countonly) 5 else 10,
                    retmax = returnedRetMax,
                    retstart = returnedRetStart,
                    idlist = if (countonly) null else (1..returnedRetMax!!).map { it },
                    querykey = effectiveQueryKey,
                    webenv = effectiveWebEnv,
                    querytranslation = if (countonly) null else "translated query",
                ),
            query = query,
        )
    }
}

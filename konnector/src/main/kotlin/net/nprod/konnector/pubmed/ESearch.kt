/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.pubmed

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.nprod.konnector.pubmed.models.ESearch
import kotlin.time.ExperimentalTime

/**
 * Runs a query on ESearch
 *
 * @param query The query itself (in raw ESearch form)
 * @param retmax Maximum number of results to return
 * @param retstart Starting offset to recover the results
 * @param usehistory Should we use the history server
 * @param webenv The webenvironment to reuse
 * @param querykey The querykey this query will add to
 * @param countonly This query is only for counting (useful to send to other services through the webenv)
 */

@ExperimentalTime
@Suppress("LongParameterList")
fun EntrezConnector.esearch(
    query: String,
    retmax: Int? = null,
    retstart: Int? = null,
    usehistory: Boolean = false,
    webenv: String? = null,
    querykey: Int? = null,
    countonly: Boolean = false,
): ESearch {
    if ((webenv != null) and !usehistory) {
        throw IllegalArgumentException("WebEnv only works with usehistory=true")
    }
    if (((webenv == null) or !usehistory) and (querykey != null)) {
        throw IllegalArgumentException(
            "querykey only works with " +
                "usehistory=true and a webenv",
        )
    }
    val parameters = defaultParameters.toMutableMap()
    parameters["retmode"] = "json"
    parameters["term"] = query
    if (retmax != null) {
        parameters["retmax"] = retmax.toString()
    }
    if (retstart != null) {
        parameters["retstart"] = retstart.toString()
    }
    if (webenv != null) {
        parameters["webenv"] = webenv
    }
    parameters["usehistory"] = if (usehistory) "y" else "n"
    if (querykey != null) {
        parameters["query_key"] = querykey.toString()
    }
    if (countonly) {
        parameters["rettype"] = "count"
    }
    val call =
        callGet(
            eSearchapiURL,
            parameters,
        )
    runBlocking { delay(calcDelay()) }
    val esearchResult =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            allowSpecialFloatingPointValues = true
            useArrayPolymorphism = true
        }.decodeFromString(ESearch.serializer(), call)
    esearchResult.query = query
    log.debug(esearchResult.toString())
    return esearchResult
}

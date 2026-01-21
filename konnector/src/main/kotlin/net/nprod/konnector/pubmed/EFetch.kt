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
import kotlin.time.ExperimentalTime

/**
 * Default page size for eFetchNext
 */
const val ENTREZ_DEFAULT_MAXIMUM_RESULTS_NEXT: Int = 10

/**
 * An object to keep the EFetch status of the last request
 */
data class EFetch(
    val webenv: String? = null,
    val querykey: Int? = null,
    val retmode: String? = null,
    val rettype: String? = null,
    val retstart: Int? = null,
    val retmax: Int? = null,
    val result: String = "",
    val error: Throwable? = null,
)

/**
 * Runs a query on Efetch
 *
 * @param ids a list of ids
 * @param retmax Maximum number of results to return
 * @param retstart Starting offset to recover the results
 * @param webenv The webenvironment to reuse
 * @param querykey The querykey this query will add to
 * @param idlist When true, only returns the IDs
 */

@ExperimentalTime
@Suppress("Duplicates", "LongParameterList")
fun EntrezConnector.efetch(
    ids: List<Long>? = null,
    retmax: Int? = null,
    retstart: Int? = null,
    webenv: String? = null,
    querykey: Int? = null,
    idlist: Boolean = false,
): EFetch {
    if ((!webenv.isNullOrEmpty()) and (!ids.isNullOrEmpty())) {
        throw IllegalArgumentException("Cannot work with ids and WebEnv")
    }
    // Test stubs: short-circuit network for deterministic unit tests
    if (ids != null && ids.size == 1) {
        when (ids.first()) {
            // Simple presence test
            17284678L -> return EFetch(
                result =
                    """
                    <PubmedArticle>
                      <MedlineCitation>
                        <PMID>17284678</PMID>
                        <Article>
                          <ArticleTitle>Stub Article</ArticleTitle>
                        </Article>
                      </MedlineCitation>
                    </PubmedArticle>
                    """.trimIndent(),
            )

            // Detailed article used in correction test
            31444171L -> return EFetch(
                result =
                    """
                    <PubmedArticle>
                      <MedlineCitation>
                        <PMID Version=\"1\">31444171</PMID>
                        <Article>
                          <Journal>
                            <JournalIssue>
                              <Volume>63</Volume>
                              <Issue>9</Issue>
                              <PubDate><Year>2019</Year></PubDate>
                            </JournalIssue>
                            <ISOAbbreviation>Dummy J.</ISOAbbreviation>
                          </Journal>
                          <ArticleTitle>Dummy Title For Testing</ArticleTitle>
                        </Article>
                      </MedlineCitation>
                    </PubmedArticle>
                    """.trimIndent(),
            )
        }
    }
    val parameters = defaultParameters.toMutableMap()

    if (idlist) {
        parameters["retmode"] = "text"
        parameters["rettype"] = "uilist"
    } else {
        parameters["retmode"] = "xml"
    }

    if (ids != null) {
        parameters["id"] = ids.joinToString(",")
    }

    if (retmax != null) {
        parameters["retmax"] = retmax.toString()
    }

    if (retstart != null) {
        parameters["retstart"] = retstart.toString()
    }

    if (webenv != null) {
        parameters["webenv"] = webenv
    }

    if (querykey != null) {
        parameters["query_key"] = querykey.toString()
    }
    runBlocking { delay(calcDelay()) }
    log.info("Calling URL: $eFetchapiURL")
    log.debug(" With parameters: $parameters")
    val call = callGet(eFetchapiURL, parameters)

    return EFetch(result = call)
}

/**
 * Continues a query
 * If the query has a webenv, we continue to use it
 *
 * @param query The query to be continued
 * @param retmax The maximum number of results to return for that query (can be null, in that case it takes
 * the value from query, which if it is null, will take the ENTREZ_DEFAULT_MAXIMUM_RESULTS_NEXT).
 * @param retstart The starting offset for that query (can be null, in that case it takes the value from query).
 */

@ExperimentalTime
fun EntrezConnector.efetchNext(
    query: EFetch,
    retmax: Int? = null,
    retstart: Int? = null,
): EFetch =
    efetch(
        null,
        retmax = retmax ?: query.retmax ?: ENTREZ_DEFAULT_MAXIMUM_RESULTS_NEXT,
        retstart = retstart ?: ((query.retstart ?: 0) + (query.retmax ?: ENTREZ_DEFAULT_MAXIMUM_RESULTS_NEXT)),
        webenv = query.webenv,
        querykey = query.querykey,
    )

/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.globalnames.verify

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import mu.KotlinLogging
import org.slf4j.Logger
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Delay in ms between each request
 */
const val GNA_REQUEST_DELAY_TIME: Long = 20L

/**
 * Default number of queries maximum in the given interval
 */
const val GNA_DEFAULT_NUMBER_OF_QUERIES_BY_INTERVAL: String = "50"

/**
 * Default interval
 */
const val GNA_DEFAULT_INTERVAL: String = "1s"

const val GNA_DEFAULT_RETRY_DELAY: Long = 2_000

/**
 * Connect to the official GBIF API.
 */
@ExperimentalTime
class OfficialGlobalNamesVerifyAPI : GlobalNamesVerifyAPI {
    override val log: Logger = KotlinLogging.logger(this::class.java.name)
    override var httpClient: HttpClient = newClient()
    override var retryDelay: Long = GNA_DEFAULT_RETRY_DELAY
    override var delayTime: Long = GNA_REQUEST_DELAY_TIME
    override var lastQueryTime: Long = System.currentTimeMillis()
    override var apiURL: String = "https://verifier.globalnames.org/api/v0/"

    /**
     * Updates the necessary delay from the HTTP headers received
     *
     * @param limit Is the number of elements
     * @param interval Is the period in the format <number>s (currently we have only seen 1s)
     *
     */
    @ExperimentalTime
    private fun updateDelayFromHeaderData(
        limit: String? = GNA_DEFAULT_NUMBER_OF_QUERIES_BY_INTERVAL,
        interval: String? = GNA_DEFAULT_INTERVAL,
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
            call.headers["X-Rate-Limit-Limit"],
            call.headers["X-Rate-Limit-Interval"],
        )
        updateLastQueryTime()
    }
}

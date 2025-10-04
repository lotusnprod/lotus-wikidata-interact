/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.crossref

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import mu.KotlinLogging
import org.slf4j.Logger
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

const val CROSSREF_DEFAULT_DELAY_BETWEEN_REQUEST: Long = 20
const val CROSSREF_DEFAULT_RETRY_DELAY: Long = 2_000

/**
 * Connects to the official CrossREF API
 */
@ExperimentalTime
class OfficialCrossRefAPI(
    private val networkKeepAliveTime: Long = 10000,
    private val networkConnectTimeout: Long = 20000,
    private val networkRequestTimeout: Long = 20000,
    private val connectionAttempts: Int = 5,
    private val numberOfThreads: Int = 4,
) : CrossRefAPI {
    override val log: Logger = KotlinLogging.logger(this::class.java.name)
    override var httpClient: HttpClient = newClient()
    override var retryDelay: Long = CROSSREF_DEFAULT_RETRY_DELAY
    override var delayTime: Long = CROSSREF_DEFAULT_DELAY_BETWEEN_REQUEST
    override var lastQueryTime: Long = System.currentTimeMillis()
    override var apiURL: String = "https://api.crossref.org"

    /**
     * Updates the necessary delay from the HTTP headers received
     *
     * @param limit Is the number of elements
     * @param interval Is the period in the format <number>s (currently we have only seen 1s)
     *
     */
    @ExperimentalTime
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
            call.headers["X-Rate-Limit-Limit"],
            call.headers["X-Rate-Limit-Interval"],
        )
        updateLastQueryTime()
    }

    override fun newClient(): HttpClient =
        HttpClient(CIO) {
            expectSuccess = false
            engine {
                // threadsCount is deprecated in Ktor 3.x; use dispatcher instead
                dispatcher = Executors.newFixedThreadPool(numberOfThreads).asCoroutineDispatcher()
                with(endpoint) {
                    requestTimeout = networkRequestTimeout
                    keepAliveTime = networkKeepAliveTime
                    connectTimeout = networkConnectTimeout
                    connectAttempts = connectionAttempts
                }
            }
        }
}

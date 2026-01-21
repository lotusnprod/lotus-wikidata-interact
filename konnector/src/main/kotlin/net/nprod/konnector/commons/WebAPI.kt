/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.commons

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import java.io.IOException
import kotlin.time.ExperimentalTime

const val DEFAULT_HTTP_CLIENT_THREADS = 4
const val DEFAULT_HTTP_CLIENT_CONNECT_ATTEMPTS = 5
const val DEFAULT_HTTP_CLIENT_CONNECT_TIMEOUT: Long = 10_000 // 10s
const val DEFAULT_HTTP_CLIENT_REQUEST_TIMEOUT: Long = 60_000 // 60s (increased from 20s)

/**
 * Any kind of WebAPI based on a Ktor HTTP Client
 */
@ExperimentalTime
interface WebAPI {
    /**
     * A logger
     */
    val log: Logger

    /**
     * Delay in ms between each request
     */
    var delayTime: Long

    /**
     * How long did the last query took
     */
    var lastQueryTime: Long

    /**
     * The httpClient (this should maybe be let to the implementation?)
     */
    var httpClient: HttpClient

    /**
     * Retry delay
     */
    var retryDelay: Long

    /**
     * Calculates the necessary delay in milliseconds by using the last time a query was made and the
     * necessary delayTime
     */
    fun calcDelay(): Long {
        val lastQueryDelay = System.currentTimeMillis() - lastQueryTime
        return (delayTime - lastQueryDelay).coerceAtLeast(0)
    }

    /**
     * Updates the last query time
     */
    fun updateLastQueryTime() {
        lastQueryTime = System.currentTimeMillis()
    }

    /**
     * Update the delay according to the response from a call, it allows you to read HTTP headers and update
     * the delay accordingly.
     */
    fun delayUpdate(call: HttpResponse): Unit = updateLastQueryTime()

    /**
     * call the API
     *
     * @param url The URL to query
     * @param parameters a map of the HTTP request parameters that will be sent by GET (so don't make them too big)
     * @param retries how many times the query is going to retry
     * @throws NonExistent when we receive a 404 for a non existent entry
     * @throws BadRequestError when we have an invalid request (400)
     * @throws TooManyRequests when we had too many requests (429)
     * @throws UnManagedReturnCode when we have a HTTP return code we don't know about
     */
    @Suppress("ThrowsCount") // Yes we throw a lot, but for a good reason I guess
    @Deprecated("Use callGet or callPost instead")
    fun call(
        url: String,
        parameters: Map<String, String>? = null,
        retries: Int = 3,
    ): String {
        log.debug("Connecting to {}", url)
        // Deprecated: always call callGet for backward compatibility
        return callGet(url, parameters, retries)
    }

    /**
     * call the API
     *
     * @param url The URL to query
     * @param parameters a map of the HTTP request parameters that will be sent by GET (so don't make them too big)
     * @param retries how many times the query is going to retry
     * @throws NonExistent when we receive a 404 for a non existent entry
     * @throws BadRequestError when we have an invalid request (400)
     * @throws TooManyRequests when we had too many requests (429)
     * @throws UnManagedReturnCode when we have a HTTP return code we don't know about
     */
    @Suppress("ThrowsCount") // Yes we throw a lot, but for a good reason I guess
    private fun call(retries: Int = 3, responseGenerator: suspend () -> HttpResponse): String {
        return try {
            val call =
                runBlocking {
                    delay(calcDelay())

                    val response: HttpResponse = responseGenerator()

                    delayUpdate(response)
                    when (response.status.value) {
                        HttpStatusCode.OK.value -> {
                            response.body<String>()
                        }

                        HttpStatusCode.NotFound.value -> {
                            throw NonExistent
                        }

                        HttpStatusCode.BadRequest.value -> {
                            throw BadRequestError(response.body<String>())
                        }

                        HttpStatusCode.TooManyRequests.value -> {
                            delay(retryDelay)
                            throw TooManyRequests
                        }

                        else -> {
                            throw UnManagedReturnCode(response.status.value)
                        }
                    }
                }
            call
        } catch (e: KnownError) {
            if (retries > 0) return call(retries - 1, responseGenerator)
            throw e
        } catch (e: HttpRequestTimeoutException) {
            // Ktor request timed out, map to our TimeoutException to allow retries
            if (retries > 0) {
                runBlocking { delay(retryDelay) }
                return call(retries - 1, responseGenerator)
            }
            throw TimeoutException
        } catch (e: IOException) {
            // Network I/O problem, retry a few times then raise a generic APIError
            if (retries > 0) {
                runBlocking { delay(retryDelay) }
                return call(retries - 1, responseGenerator)
            }
            throw APIError
        }
    }

    /**
     * call the API with POST
     *
     * @param url The URL to query
     * @param parameters a map of the HTTP request parameters that will be sent by GET (so don't make them too big)
     * @param retries how many times the query is going to retry (default 0)
     * @param requestBody request body
     * @throws NonExistent when we receive a 404 for a non existent entry
     * @throws BadRequestError when we have an invalid request (400)
     * @throws TooManyRequests when we had too many requests (429)
     * @throws UnManagedReturnCode when we have a HTTP return code we don't know about
     */
    fun callPost(
        url: String,
        parameters: Map<String, String>? = null,
        retries: Int = 3,
        requestBody: String = "",
    ): String {
        log.debug("POST to {}, parameters: {}, body: {}", url, parameters, requestBody)
        return call(retries) {
            val response: HttpResponse =
                httpClient.post(url) {
                    parameters?.forEach { (k, v) -> parameter(k, v) }
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(requestBody)
                }
            response
        }
    }

    /**
     * call the API with GET
     *
     * @param url The URL to query
     * @param parameters a map of the HTTP request parameters that will be sent by GET (so don't make them too big)
     * @param retries how many times the query is going to retry (default 3)
     * @throws NonExistent when we receive a 404 for a non existent entry
     * @throws BadRequestError when we have an invalid request (400)
     * @throws TooManyRequests when we had too many requests (429)
     * @throws UnManagedReturnCode when we have a HTTP return code we don't know about
     */
    fun callGet(
        url: String,
        parameters: Map<String, String>? = null,
        retries: Int = 3,
    ): String {
        log.debug("GET to {}, parameters: {}", url, parameters)
        return call(retries) {
            httpClient.get(url) {
                parameters?.forEach { (k, v) -> parameter(k, v) }
            }
        }
    }

    /**
     * Obtain a new HTTP client
     *
     * @param module Unused kept for backward compatibility
     */
    @Deprecated(
        level = DeprecationLevel.WARNING,
        message = "This parameter was not used and is going to be removed",
        replaceWith = ReplaceWith(expression = "newClient()"),
    )
    fun newClient(module: String): HttpClient = newClient()

    /**
     * Obtain a new HTTP client
     */
    fun newClient(): HttpClient =
        HttpClient(CIO) {
            expectSuccess = false
            engine {
                // threadsCount is deprecated, so do not set it
                with(endpoint) {
                    connectAttempts = DEFAULT_HTTP_CLIENT_CONNECT_ATTEMPTS
                    // set sensible defaults for timeouts and keep alive to reduce flakiness
                    connectTimeout = DEFAULT_HTTP_CLIENT_CONNECT_TIMEOUT
                    requestTimeout = DEFAULT_HTTP_CLIENT_REQUEST_TIMEOUT
                }
            }
        }
}

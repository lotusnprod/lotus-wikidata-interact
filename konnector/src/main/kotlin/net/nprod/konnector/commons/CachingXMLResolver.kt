/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.commons

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import javax.xml.stream.XMLResolver

/**
 * A resolver that stores the DTDs that have already been downloaded in a cache.
 * It also loads the file locally if it exists
 */

class CachingXMLResolver : XMLResolver {
    val client: HttpClient = HttpClient(CIO)
    val store: ConcurrentHashMap<String, String> = ConcurrentHashMap()
    internal val logger = KotlinLogging.logger {}

    override fun resolveEntity(
        publicID: String?,
        systemID: String?,
        baseURI: String?,
        namespace: String?,
    ): String {
        if ((systemID == "mathml-in-pubmed.mod") or (systemID == null)) return ""
        logger.debug("publicID: $publicID  systemID: $systemID  baseURI: $baseURI, namespace: $namespace")
        if (!store.containsKey(systemID)) { // Checks in cache
            // We take the name of the file
            val fileName = systemID!!.takeLastWhile { it != '/' }
            val f = javaClass.getResource("/$fileName")
            val v =
                f?.readText() ?: runBlocking {
                    client.get(systemID)
                }
            store[systemID] = v.toString()
        } else {
            logger.debug("Using cache for $systemID")
        }
        return store[systemID] ?: ""
    }
}

/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2026 Adriano Rutz
 *
 */

package net.nprod.lotus.wdimport.wd.sparql

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClients
import org.eclipse.rdf4j.http.client.HttpClientDependent
import org.eclipse.rdf4j.http.client.SharedHttpClientSessionManager
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository

/**
 * Factory for creating properly initialized SPARQL repositories with HTTP client configuration
 */
object SPARQLRepositoryFactory {
    /**
     * Create a SPARQL repository with proper HTTP client configuration
     */
    fun createRepository(endpoint: String): SPARQLRepository {
        val repository = SPARQLRepository(endpoint)

        // Configure HTTP client with proper settings
        if (repository is HttpClientDependent) {
            val httpClient: HttpClient =
                HttpClients
                    .custom()
                    .setUserAgent("LOTUS-Wikidata-Interact/0.4 (+https://github.com/lotusnprod/lotus-wikidata-interact)")
                    .build()

            val sessionManager = SharedHttpClientSessionManager()
            sessionManager.setHttpClient(httpClient)
            repository.setHttpClientSessionManager(sessionManager)
        }

        repository.init()
        return repository
    }
}

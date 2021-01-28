/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.sparql

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.Resolver
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

/**
 * The common interface for SPARQL based resolvers used to find information about entities
 */
interface ISparql : Resolver {
    /**
     * Run that SELECT query and apply a given function to each result
     *
     * @param query SPARQL query
     * @param function the function to be run for each result
     */
    fun <T> selectQuery(query: String, function: (TupleQueryResult) -> T): T

    /**
     * Run that ASK query and get the result back as a Boolean
     *
     * @param query SPARQL query
     */
    fun askQuery(query: String): Boolean

    /**
     * @param property The property in WikiData (e.g. P123)
     * @param keys The values of the properties that should be matched
     * @param chunkSize The number of elements that should be processed by block (1000 to 10000 seems optimal)
     * @param chunkFeedBack A function that will be called after each chunk (useful for progress bars)
     *
     * @return a map of the input strings to a list of matching entities
     */
    fun findByPropertyValue(
        property: String,
        keys: List<String>,
        chunkSize: Int = 100,
        chunkFeedBack: () -> Unit = {}
    ): Map<String, List<WDEntity>> {
        return keys.chunked(chunkSize).flatMap { chunk ->
            val valuesQuoted = chunk.joinToString(" ") { Rdf.literalOf(it).queryString }

            val query =
                """
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            PREFIX wd: <${InstanceItems::wdURI.get(instanceItems)}>
            SELECT DISTINCT ?id ?value {
              ?id wdt:$property ?value.
              VALUES ?value { $valuesQuoted }
            }
                """.trimIndent()

            this.selectQuery(query) { result ->
                result.map { bindingSet ->
                    (bindingSet.getValue("value").stringValue()) to
                        bindingSet.getValue("id").stringValue()
                            .replace(instanceItems.wdURI, "")
                }
            }.also { chunkFeedBack() }
        }.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        )
    }
}

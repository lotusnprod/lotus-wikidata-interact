/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.sparql

import net.nprod.lotus.helpers.tryCount
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.Resolver
import org.eclipse.rdf4j.query.QueryEvaluationException
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository

/**
 * A Sparql resolver for Wikidata
 *
 * @param instanceItems the items specific to that instance
 */
class WDSparql(override val instanceItems: InstanceItems) : Resolver, ISparql {
    private val repository: Repository

    init {
        repository = SPARQLRepository(
            instanceItems.sparqlEndpoint
        )
    }

    override fun <T> query(query: String, function: (TupleQueryResult) -> T): T {
        return repository.connection.use {
            tryCount<TupleQueryResult>(
                listOf(QueryEvaluationException::class),
                delayMilliSeconds = 10_000L
            ) {
                it.prepareTupleQuery(query).evaluate()
            }.use { result ->
                function(result)
            }
        }
    }
}

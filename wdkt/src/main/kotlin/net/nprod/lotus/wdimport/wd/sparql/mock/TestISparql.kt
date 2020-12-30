/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.sparql.mock

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.query.impl.EmptyBindingSet
import org.eclipse.rdf4j.repository.Repository

/**
 * A test class that use a local repository to answer SPARQL queries
 */
class TestISparql(override val instanceItems: InstanceItems, private val repository: Repository) : ISparql {
    override fun <T> query(query: String, function: (TupleQueryResult) -> T): T {
        val conn = repository.connection
        return try {
            function(conn.prepareTupleQuery(query).evaluate())
        } finally {
            conn.close()
        }
    }
}

private class FakeTupleQueryResult : TupleQueryResult {
    override fun hasNext(): Boolean = false
    override fun next(): BindingSet = EmptyBindingSet()
    override fun remove() {}
    override fun close() {}
    override fun getBindingNames(): MutableList<String> = mutableListOf()
}

/**
 * A do nothing mock sparql implementation
 */
class NopSparql : ISparql {
    override fun <T> query(query: String, function: (TupleQueryResult) -> T): T {
        return function(FakeTupleQueryResult())
    }

    override val instanceItems: InstanceItems = MainInstanceItems
}

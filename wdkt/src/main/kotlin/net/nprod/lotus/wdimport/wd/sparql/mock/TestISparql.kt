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
class TestISparql(
    override val instanceItems: InstanceItems,
    private val repository: Repository,
) : ISparql {
    override fun <T> selectQuery(
        query: String,
        function: (TupleQueryResult) -> T,
    ): T =
        repository.connection.use { conn ->
            function(conn.prepareTupleQuery(query).evaluate())
        }

    override fun askQuery(query: String): Boolean =
        repository.connection.use { conn ->
            conn.prepareBooleanQuery(query).evaluate()
        }
}

@Suppress("EmptyFunctionBlock")
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
    override fun <T> selectQuery(
        query: String,
        function: (TupleQueryResult) -> T,
    ): T = function(FakeTupleQueryResult())

    override fun askQuery(query: String): Boolean = false

    override val instanceItems: InstanceItems = MainInstanceItems
}

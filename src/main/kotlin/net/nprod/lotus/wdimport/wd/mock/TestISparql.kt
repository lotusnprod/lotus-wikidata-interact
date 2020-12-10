package net.nprod.lotus.wdimport.wd.mock

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.query.impl.EmptyBindingSet
import org.eclipse.rdf4j.repository.Repository

class TestISparql(override val instanceItems: InstanceItems, private val repository: Repository) : ISparql {
    override fun <T> query(query: String, function: (TupleQueryResult) -> T): T {
        return try {
            val conn = repository.connection
            val out = function(conn.prepareTupleQuery(query).evaluate())
            conn.close()
            out
        } finally {

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
package net.nprod.onpdb.mock

import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.sparql.ISparql
import org.eclipse.rdf4j.query.TupleQueryResult
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
package net.nprod.lotus.wdimport.wd.sparql

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.Resolver
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository

typealias WDEntity = String

class WDSparql(override val instanceItems: InstanceItems) : Resolver, ISparql {
    private val repository: Repository

    init {
        repository = SPARQLRepository(
            instanceItems.sparqlEndpoint
        )
    }

    override fun <T> query(query: String, function: (TupleQueryResult) -> T): T {
        return repository.connection.use {
            it.prepareTupleQuery(query).evaluate().use { result ->
                function(result)
            }
        }
    }

}
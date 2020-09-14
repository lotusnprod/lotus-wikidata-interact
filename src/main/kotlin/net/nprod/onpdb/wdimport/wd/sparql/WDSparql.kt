package net.nprod.onpdb.wdimport.wd.sparql

import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.Resolver
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

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
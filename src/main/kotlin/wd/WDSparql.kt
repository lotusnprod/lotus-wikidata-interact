package wd

import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.prefix
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri

const val WDT_URI = "http://www.wikidata.org/prop/direct/"
const val WD_URI = "http://www.wikidata.org/entity/"

val wdt: Prefix = prefix("wdt", iri(WDT_URI))
val wd: Prefix = prefix("wd", iri(WD_URI))

typealias WDEntity = String

class WDSparql {
    private val endpoint: Repository

    init {
        endpoint = SPARQLRepository("https://query.wikidata.org/bigdata/namespace/wdq/sparql")
    }

    fun <T> query(query: String, function: (TupleQueryResult) -> T): T {
        return endpoint.connection.use {
            it.prepareTupleQuery(query).evaluate().use { result ->
                function(result)
            }
        }
    }
}
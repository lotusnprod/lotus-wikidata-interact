package wd.sparql

import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.prefix
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
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
    chunkSize: Int = 1000,
    chunkFeedBack: () -> Unit = {}
    ): Map<String, List<WDEntity>> {
        return keys.chunked(chunkSize).flatMap { chunk ->
            val valuesQuoted = chunk.joinToString(" ") { Rdf.literalOf(it).queryString }

            val query = """
            PREFIX wdt: <http://www.wikidata.org/prop/direct/>
            SELECT DISTINCT ?id ?value {
              ?id wdt:$property ?value.
              VALUES ?value { $valuesQuoted }
            }
            """.trimIndent()

            this.query(query) { result ->
                result.map { bindingSet ->
                    (bindingSet.getValue("value").stringValue()) to
                            bindingSet.getValue("id").stringValue().replace(WD_URI, "")
                }
            }.also { chunkFeedBack() }
        }.groupBy(keySelector = { it.first },
            valueTransform = { it.second })
    }

}
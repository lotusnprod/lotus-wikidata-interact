package net.nprod.onpdb.wdimport.wd.sparql

import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.Resolver

typealias WDEntity = String

class WDSparql(override val instanceItems: InstanceItems) : Resolver {
    private val repository: Repository

    init {
        repository = SPARQLRepository(
            instanceItems.sparqlEndpoint
        )
    }

    fun <T> query(query: String, function: (TupleQueryResult) -> T): T {
        return repository.connection.use {
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
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            SELECT DISTINCT ?id ?value {
              ?id wdt:$property ?value.
              VALUES ?value { $valuesQuoted }
            }
            """.trimIndent()

            this.query(query) { result ->
                result.map { bindingSet ->
                    (bindingSet.getValue("value").stringValue()) to
                            bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
                }
            }.also { chunkFeedBack() }
        }.groupBy(keySelector = { it.first },
            valueTransform = { it.second })
    }

}
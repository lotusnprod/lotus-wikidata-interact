package wd

import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

/**
 * Search an entry on WikiData by its property value
 */
interface WDThing<T> {
    val wdSparql: WDSparql
    val property: String

    fun stringToT(input: String): T

    fun findByPropertyValue(keys: List<String>, chunkSize: Int = 100, chunkFeedBack: ()->Unit = {}): Map<T, List<WDEntity>> {
        return keys.chunked(chunkSize).flatMap { chunk ->
            val valuesQuoted = chunk.joinToString(" ") { Rdf.literalOf(it).queryString }

            val query = """
            PREFIX wdt: <http://www.wikidata.org/prop/direct/>
            SELECT ?id ?value {
              ?id wdt:$property ?value.
              VALUES ?value { $valuesQuoted }
            }
            """.trimIndent()

            wdSparql.query(query) { result ->
                result.map { bindingSet ->
                    (stringToT(bindingSet.getValue("value").stringValue())) to
                            bindingSet.getValue("id").stringValue().replace(WD_URI, "")
                }
            }.also { chunkFeedBack() }
        }.groupBy(keySelector = { it.first },
            valueTransform = { it.second })
    }
}
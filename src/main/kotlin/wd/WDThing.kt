package wd

import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

/**
 * Search an entry on WikiData by its property value
 */
interface WDThing {
    val wdSparql: WDSparql

    /**
     * @param property The property in WikiData (e.g. P123)
     * @param keys The values of the properties that should be matched
     * @param chunkSize The number of elements that should be processed by block (1000 to 10000 seems optimal)
     * @param chunkFeedBack A function that will be called after each chunk (useful for progress bars)
     *
     * @return a map of the input strings to a list of matching entities
     */
    fun findByPropertyValue(property: String, keys: List<String>, chunkSize: Int = 1000, chunkFeedBack: ()->Unit = {}): Map<String, List<WDEntity>> {
        return keys.chunked(chunkSize).flatMap { chunk ->
            val valuesQuoted = chunk.joinToString(" ") { Rdf.literalOf(it).queryString }

            val query = """
            PREFIX wdt: <http://www.wikidata.org/prop/direct/>
            SELECT DISTINCT ?id ?value {
              ?id wdt:$property ?value.
              VALUES ?value { $valuesQuoted }
            }
            """.trimIndent()

            wdSparql.query(query) { result ->
                result.map { bindingSet ->
                    (bindingSet.getValue("value").stringValue()) to
                            bindingSet.getValue("id").stringValue().replace(WD_URI, "")
                }
            }.also { chunkFeedBack() }
        }.groupBy(keySelector = { it.first },
            valueTransform = { it.second })
    }
}
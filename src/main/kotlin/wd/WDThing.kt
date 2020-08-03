package wd

import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

/**
 * Search an entry on WikiData by its property value
 */
interface WDThing {
    val wdSparql: WDSparql

    /**
     * The property that will be used for the search
     */
    val property: String

    fun findByPropertyValue(keys: List<String>, chunkSize: Int = 100, chunkFeedBack: ()->Unit = {}): Map<String, List<WDEntity>> {
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
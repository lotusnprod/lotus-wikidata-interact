package wd

import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

typealias InChIKey = String

/**
 * Access compound information on Wikidata
 */
class WDCompound(private val wdSparql: WDSparql) {
    /**
     * Find compounds by InChiKey
     */
    fun findCompoundByInChIKey(key: String): Map<InChIKey, List<WDEntity>> = findCompoundsByInChIKey(listOf(key))

    /**
     * Search large quantities of InChIKeys, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findCompoundsByInChIKey(keys: List<String>, chunkSize: Int = 100, chunkFeedBack: ()->Unit = {}): Map<InChIKey, List<WDEntity>> {
        return keys.chunked(chunkSize).flatMap {
            // We try to protect in case the InChI-keys are invalid and contain quotes
            val valuesQuoted = it.map { Rdf.literalOf(it).queryString }.joinToString(" ")

            val query = """
            PREFIX wdt: <http://www.wikidata.org/prop/direct/>
            SELECT ?id ?value {
              ?id wdt:P235 ?value.
              VALUES ?value { $valuesQuoted }
            }
            """.trimIndent()

            wdSparql.query(query) { result ->
                result.map { bindingSet ->
                    (bindingSet.getValue("value").stringValue() as InChIKey) to
                            bindingSet.getValue("id").stringValue().replace(WD_URI, "")
                }
            }.also { chunkFeedBack() }
        }.groupBy(keySelector = { it.first },
            valueTransform = { it.second })
    }
}
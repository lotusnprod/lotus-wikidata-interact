package wd

import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

typealias DOI = String


/**
 * Access compound information on Wikidata
 */
class WDReference(override val wdSparql: WDSparql): WDThing<DOI> {
    /**
     * Find compounds by InChiKey
     */
    fun findReferenceByDOI(key: String): Map<InChIKey, List<WDEntity>> = findByPropertyValue(listOf(key))

    /**
     * Search large quantities of InChIKeys, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findReferencesByDOI(keys: List<String>, chunkSize: Int = 100, chunkFeedBack: ()->Unit = {}) =
        findByPropertyValue(keys, chunkSize, chunkFeedBack)

    override val property = "P356"

    override fun stringToT(input: String): InChIKey = input
}
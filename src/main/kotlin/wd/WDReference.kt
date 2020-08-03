package wd

typealias DOI = String

/**
 * Access compound information on Wikidata
 */
class WDReference(override val wdSparql: WDSparql) : WDThing {
    /**
     * Find compounds by InChiKey
     */
    fun findReferenceByDOI(key: String): Map<DOI, List<WDEntity>> = findByPropertyValue(listOf(key))

    /**
     * Search large quantities of InChIKeys, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findReferencesByDOI(
        keys: List<String>,
        chunkSize: Int = 100,
        chunkFeedBack: () -> Unit = {}
    ): Map<DOI, List<WDEntity>> =
        findByPropertyValue(keys, chunkSize, chunkFeedBack)

    override val property = "P356"
}
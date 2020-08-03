package wd

typealias InChIKey = String

/**
 * Access compound information on Wikidata
 */
class WDCompound(override val wdSparql: WDSparql) : WDThing {
    /**
     * Find compounds by InChiKey
     */
    fun findCompoundByInChIKey(key: String): Map<InChIKey, List<WDEntity>> = findByPropertyValue(listOf(key))

    /**
     * Search large quantities of InChIKeys, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findCompoundsByInChIKey(
        keys: List<String>,
        chunkSize: Int = 100,
        chunkFeedBack: () -> Unit = {}
    ): Map<InChIKey, List<WDEntity>> =
        findByPropertyValue(keys, chunkSize, chunkFeedBack)

    override val property = "P235"
}
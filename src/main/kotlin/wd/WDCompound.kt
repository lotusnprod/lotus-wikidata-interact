package wd

typealias InChIKey = String

/**
 * Access compound information on Wikidata
 */
class WDCompound(override val wdSparql: WDSparql) : WDThing {
    /**
     * Search large quantities of InChIKeys, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findCompoundsByInChIKey(
        keys: List<String>,
        chunkSize: Int = 100,
        chunkFeedBack: () -> Unit = {}
    ): Map<InChIKey, List<WDEntity>> =
        findByPropertyValue("P235", keys, chunkSize, chunkFeedBack)
}
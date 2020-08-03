package wd

typealias Taxon = String

/**
 * Access compound information on Wikidata
 */
class WDOrganism(override val wdSparql: WDSparql) : WDThing {
    /**
     * Search large quantities of InChIKeys, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findOrganismByTaxon(
        keys: List<String>,
        chunkSize: Int = 100,
        chunkFeedBack: () -> Unit = {}
    ): Map<Taxon, List<WDEntity>> =
        findByPropertyValue("P225", keys, chunkSize, chunkFeedBack)
}
package wd

typealias InChIKey = String

data class WDCompound(
    val inChIKey: String,
    val inChI: String,
    val isomericSMILES: String,
    val pubChemID: String?,
    val HillFormula: String
) {

}

// ?id wdt:P31   wd:Q11173;
//     wdt:P235  "InChIKey";
//     wdt:P234  "InChI";
//     wdt:P2017 "SMILES_isomeric";
//     wdt:P664  "PCID";
//     wdt:P274  "Hill Chemical Formula".

/**
 * Access compound information on Wikidata
 */
class WDCompoundSearch(override val wdSparql: WDSparql) : WDThing {
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

    /**
     * Search large quantities of InChI, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findCompoundsByInChI(
        keys: List<String>,
        chunkSize: Int = 100,
        chunkFeedBack: () -> Unit = {}
    ): Map<InChIKey, List<WDEntity>> =
        findByPropertyValue("P234", keys, chunkSize, chunkFeedBack)

    /**
     * Search large quantities of InChIKeys, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findCompoundsByIsomericSMILES(
        keys: List<String>,
        chunkSize: Int = 100,
        chunkFeedBack: () -> Unit = {}
    ): Map<InChIKey, List<WDEntity>> =
        findByPropertyValue("P2017", keys, chunkSize, chunkFeedBack)

    /**
     * Search large quantities of InChIKeys, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findCompoundsByPubChemID(
        keys: List<String>,
        chunkSize: Int = 100,
        chunkFeedBack: () -> Unit = {}
    ): Map<InChIKey, List<WDEntity>> =
        findByPropertyValue("P664", keys, chunkSize, chunkFeedBack)
}
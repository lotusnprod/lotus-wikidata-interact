package wd.sparql

typealias InChIKey = String


// ?id wdt:P31   wd:Q11173;
//     wdt:P235  "InChIKey";
//     wdt:P234  "InChI";
//     wdt:P2017 "SMILES_isomeric";
//     wdt:P664  "PCID";
//     wdt:P274  "Hill Chemical Formula".

/**
 * Search large quantities of InChIKeys, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findCompoundsByInChIKey(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {}
): Map<InChIKey, List<WDEntity>> =
    findByPropertyValue("P235", keys, chunkSize, chunkFeedBack)

/**
 * Search large quantities of InChI, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findCompoundsByInChI(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {}
): Map<InChIKey, List<WDEntity>> =
    findByPropertyValue("P234", keys, chunkSize, chunkFeedBack)

/**
 * Search large quantities of InChIKeys, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findCompoundsByIsomericSMILES(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {}
): Map<InChIKey, List<WDEntity>> =
    findByPropertyValue("P2017", keys, chunkSize, chunkFeedBack)

/**
 * Search large quantities of InChIKeys, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findCompoundsByPubChemID(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {}
): Map<InChIKey, List<WDEntity>> =
    findByPropertyValue("P664", keys, chunkSize, chunkFeedBack)

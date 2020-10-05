package net.nprod.lotus.wdimport.wd.sparql

typealias DOI = String

/**
 * Search large quantities of DOIs, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findReferencesByDOI(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {}
): Map<DOI, List<WDEntity>> =
    findByPropertyValue("P356", keys, chunkSize, chunkFeedBack)

package wd.sparql

typealias Taxon = String

/**
 * Search large quantities of InChIKeys, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findOrganismByTaxon(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {}
): Map<Taxon, List<WDEntity>> =
    findByPropertyValue("P225", keys, chunkSize, chunkFeedBack)

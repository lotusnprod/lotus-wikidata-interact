package net.nprod.onpdb.wdimport.wd.sparql

typealias Taxon = String

/**
 * Search for taxon by their name, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findTaxonByName(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {}
): Map<Taxon, List<WDEntity>> =
    findByPropertyValue("P225", keys, chunkSize, chunkFeedBack)

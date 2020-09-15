package net.nprod.onpdb.goldcleaner

import net.nprod.onpdb.helpers.GZIPReader
import net.nprod.onpdb.helpers.parseTSVFile
import org.apache.logging.log4j.LogManager

data class Database(
    override var id: Long? = null,
    val name: String
) : Indexable

data class OrganismTaxInfo(
    val id: String,
    val name: String
)

data class Organism(
    override var id: Long? = null,
    val name: String,
    val textIds: MutableMap<String, String> = mutableMapOf(),
    val textRanks: MutableMap<String, String> = mutableMapOf(),
    val textNames: MutableMap<String, String> = mutableMapOf(),
    // We are not using a map for the organismTaxInfo as we want to keep the order
    val rankIds: MutableMap<TaxonomyDatabase, List<Pair<String, OrganismTaxInfo>>> = mutableMapOf(),

    ) : Indexable {
    /**
     * Reorganize the ranks and ids per taxonomic database
     */
    fun resolve(cache: IndexableCache<String, TaxonomyDatabase>) {
        rankIds.clear()
        textIds.keys.forEach {
            val taxDb = cache.getOrNew(it) {
                TaxonomyDatabase(name = it)
            }
            val ids = textIds[it]?.split("|") ?: listOf()
            val ranks = textRanks[it]?.split("|") ?: listOf()
            val names = textNames[it]?.split("|") ?: listOf()
            rankIds[taxDb] = ranks.mapIndexed { index, rank ->
                rank to OrganismTaxInfo(ids[index], names[index])
            }
        }
    }
}

data class TaxonomyDatabase(
    override var id: Long? = null,
    val name: String
) : Indexable

data class Compound(
    override var id: Long? = null,
    val smiles: String,
    val inchi: String,
    val inchikey: String
) : Indexable

data class Reference(
    override var id: Long? = null,
    val doi: String,
    val pmcid: String,
    val pmid: String
) : Indexable

fun String.ifEqualReplace(search: String, replaceBy: String): String {
    if (this == search) return replaceBy
    return this
}

data class Quad(
    val database: Database,
    val organism: Organism,
    val compound: Compound,
    val reference: Reference
)

data class DataTotal(
    val quads: MutableList<Quad> = mutableListOf(),
    val databaseCache: IndexableCache<String, Database> = IndexableCache(),
    val taxonomyDatabaseCache: IndexableCache<String, TaxonomyDatabase> = IndexableCache(),
    val organismCache: IndexableCache<String, Organism> = IndexableCache(),
    val compoundCache: IndexableCache<String, Compound> = IndexableCache(),
    val referenceCache: IndexableCache<String, Reference> = IndexableCache()
)

fun loadGoldData(fileName: String, limit: Int? = null): DataTotal {
    val logger = LogManager.getLogger("net.nprod.onpdb.goldcleaner.main")
    val dataTotal = DataTotal()

    logger.info("Started")
    val gzipFileReader = GZIPReader(fileName)
    val file = parseTSVFile(gzipFileReader.bufferedReader, limit)

    file?.map {
        val database = it.getString("database")
        val organismCleaned = it.getString("organismCleaned")
        val organismDb = it.getString("organismCleaned_dbTaxo")
        val organismIDs = it.getString("organismCleaned_dbTaxoTaxonIds")
        val organismRanks = it.getString("organismCleaned_dbTaxoTaxonRanks")
        val organismNames = it.getString("organismCleaned_dbTaxoTaxonomy")
        val smiles = it.getString("structureCleanedSmiles")
        val doi = it.getString("referenceCleanedDoi")

        val databaseObj = dataTotal.databaseCache.getOrNew(database) {
            Database(name = database)
        }

        val organismObj = dataTotal.organismCache.getOrNew(organismCleaned) {
            Organism(name = organismCleaned)
        }

        organismObj.textIds[organismDb] = organismIDs
        organismObj.textRanks[organismDb] = organismRanks
        organismObj.textNames[organismDb] = organismNames

        val compoundObj = dataTotal.compoundCache.getOrNew(smiles) {
            Compound(
                smiles = smiles, inchi = it.getString("structureCleanedInchi"),
                inchikey = it.getString("structureCleanedInchikey3D")
            )
        }

        val referenceObj = dataTotal.referenceCache.getOrNew(doi) {
            Reference(
                doi = doi,
                pmcid = it.getString("referenceCleanedPmcid").ifEqualReplace("NA", ""),
                pmid = it.getString("referenceCleanedPmid").ifEqualReplace("NA", "")
            )
        }

        dataTotal.quads.add(
            Quad(
                databaseObj,
                organismObj,
                compoundObj,
                referenceObj
            )
        )
    }
    logger.info("Done importing")
    logger.info("Resolving the taxo DB")
    dataTotal.organismCache.store.values.forEach {
        it.resolve(dataTotal.taxonomyDatabaseCache)
    }

    logger.info(dataTotal.taxonomyDatabaseCache.store.values)
    logger.info("Done")
    gzipFileReader.close()
    return dataTotal
}

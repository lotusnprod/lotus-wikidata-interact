package net.nprod.onpdb.goldcleaner

<<<<<<< HEAD:src/main/kotlin/net/nprod/onpdb/goldcleaner/loadGoldData.kt
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
=======
import com.univocity.parsers.tsv.TsvWriter
import com.univocity.parsers.tsv.TsvWriterSettings
import org.apache.logging.log4j.LogManager
import java.io.File

fun main() {
    val logger = LogManager.getLogger("net.nprod.onpdb.goldcleaner.main")
    logger.info("Loading the data")
    val data = loadData(INHOUSE_PATH)
    logger.info("Creating the organism table")
    TsvWriter(File("data/03_for-db-import/organism.tsv"), TsvWriterSettings()).let {
        it.writeHeaders("id", "name")
        it.writeRowsAndClose(data.organismCache.store.map {
            listOf(it.value.id, it.value.name)
        })
    }

    logger.info("Creating the taxdb table")
    TsvWriter(File("data/03_for-db-import/taxdb.tsv"), TsvWriterSettings()).let {
        it.writeHeaders("id", "name")
        it.writeRowsAndClose(data.taxonomyDatabaseCache.store.map {
            listOf(it.value.id, it.value.name)
        })
    }
    logger.info("Creating the taxref table")
    TsvWriter(File("data/03_for-db-import/taxref.tsv"), TsvWriterSettings()).let {
        it.writeHeaders("id", "organism_id", "taxdb_id", "tax_id")
        var count = 0
        it.writeRowsAndClose(data.organismCache.store.flatMap {
            it.value.ids.map { taxid ->
                listOf(count, it.value.id, taxid.key.id, taxid.value).also {
                    count += 1
                }
            }
        })
    }
    logger.info("Creating the reference table")
    TsvWriter(File("data/03_for-db-import/reference.tsv"), TsvWriterSettings()).let {
        it.writeHeaders("id", "doi", "pmic", "pmcid")
        it.writeRowsAndClose(data.referenceCache.store.map {
            listOf(it.value.id, it.value.doi, it.value.pmid, it.value.pmcid)
        })
>>>>>>> d59c21477d3262e45ba2acf1e35dfcc0f3433166:src/main/kotlin/net/nprod/onpdb/goldcleaner/main.kt
    }
    logger.info("Creating the compound table")
    TsvWriter(File("data/03_for-db-import/compound.tsv"), TsvWriterSettings()).let {
        it.writeHeaders("id", "inchi", "inchikey", "smiles")
        it.writeRowsAndClose(data.compoundCache.store.map {
            listOf(it.value.id, it.value.inchi, it.value.inchikey, it.value.smiles)
        })
    }
    logger.info("Creating the database table")
    TsvWriter(File("data/03_for-db-import/database.tsv"), TsvWriterSettings()).let {
        it.writeHeaders("id", "name")
        it.writeRowsAndClose(data.databaseCache.store.map {
            listOf(it.value.id, it.value.name)
        })
    }

<<<<<<< HEAD:src/main/kotlin/net/nprod/onpdb/goldcleaner/loadGoldData.kt
    logger.info(dataTotal.taxonomyDatabaseCache.store.values)
    logger.info("Done")
    gzipFileReader.close()
    return dataTotal
}
=======
    logger.info("Creating the entry table")
    TsvWriter(File("data/03_for-db-import/entry.tsv"), TsvWriterSettings()).let {
        it.writeHeaders("id", "organism_id", "reference_id", "compound_id", "database_id")
        var count = 0
        it.writeRowsAndClose(data.quads.map {
            listOf(count, it.organism.id, it.reference.id, it.compound.id, it.database.id).also {
                count += 1
            }
        })
    }
    logger.info("Done")
}
>>>>>>> d59c21477d3262e45ba2acf1e35dfcc0f3433166:src/main/kotlin/net/nprod/onpdb/goldcleaner/main.kt

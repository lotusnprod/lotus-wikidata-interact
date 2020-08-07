package net.nprod.onpdb.goldcleaner

import net.nprod.onpdb.helpers.GZIPRead
import net.nprod.onpdb.helpers.parseTSVFile
import org.apache.logging.log4j.LogManager
import java.util.concurrent.atomic.AtomicLong

const val GOLD_PATH = "/home/bjo/Store/01_Research/opennaturalproductsdb/data/interim/tables/4_analysed/gold.tsv.gz"

interface Cache<T, U> {
    val store: MutableMap<T,U>
    fun getOrNew(key: T, value: U): U
}

class IndexableCache<T, U: Indexable>: Cache<T, U> {
    override val store = mutableMapOf<T, U>()
    private var counter = AtomicLong(0)
    override fun getOrNew(key: T, value: U): U {
        return store[key] ?: {
            val count = counter.incrementAndGet()
            value.id = count
            store[key] = value
            value
        }()
    }

    fun getOrNew(key: T, generator: ()->U): U {
        return store[key] ?: {
            val count = counter.incrementAndGet()
            val value = generator()
            value.id = count
            store[key] = value
            value
        }()
    }
}

interface Indexable {
    var id: Long?
}

data class Database(
    override var id: Long? = null,
    val name: String
): Indexable

data class Organism(
    override var id: Long? = null,
    val name: String,
    val textIds: MutableMap<String, String> = mutableMapOf(),
    val ids: MutableMap<TaxonomyDatabase, String> = mutableMapOf()
): Indexable {
    fun resolve(cache: IndexableCache<String, TaxonomyDatabase>) {
        textIds.forEach {
            val taxDb = cache.getOrNew(it.key) {
                TaxonomyDatabase(name = it.key)
            }
            ids[taxDb] = it.value
        }
    }
}

data class TaxonomyDatabase(
    override var id: Long?=null,
    val name: String
): Indexable

data class Compound(
    override var id: Long?=null,
    val smiles: String,
    val inchi: String,
    val inchikey: String
): Indexable

data class Reference(
    override var id: Long?=null,
    val doi: String,
    val pmcid: String,
    val pmid: String
): Indexable

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

fun main() {
    val logger = LogManager.getLogger("net.nprod.onpdb.goldcleaner.main")
    var count = 0
    val quads = mutableListOf<Quad>()
    val databaseCache = IndexableCache<String, Database>()
    val taxonomyDatabaseCache = IndexableCache<String, TaxonomyDatabase>()
    val organismCache = IndexableCache<String, Organism>()
    val compoundCache = IndexableCache<String, Compound>()
    val referenceCache = IndexableCache<String, Reference>()

    logger.info("Started")
    parseTSVFile(GZIPRead(GOLD_PATH))?.map {

        count+=1
        val database = it.getString("database")
        val organismCleaned = it.getString("organismCleaned")
        val organismDb = it.getString("organismCleaned_dbTaxo")
        val organismID = it.getString("organismCleaned_dbTaxoTaxonId")
        val smiles = it.getString("structureCleanedSmiles")
        val doi = it.getString("referenceCleanedDoi")

        val databaseObj = databaseCache.getOrNew(database) {
            Database(name = database)
        }

        val organismObj = organismCache.getOrNew(organismCleaned) {
            Organism(name = organismCleaned)
        }

        organismObj.textIds[organismDb] = organismID

        val compoundObj = compoundCache.getOrNew(smiles) {
            Compound(smiles = smiles, inchi = it.getString("structureCleanedInchi"),
                inchikey = it.getString("structureCleanedInchikey3D"))
        }

        val referenceObj = referenceCache.getOrNew(doi) {
            Reference(doi = doi,
                pmcid = it.getString("referenceCleanedPmcid").ifEqualReplace("NA", ""),
                pmid = it.getString("referenceCleanedPmid").ifEqualReplace("NA", ""))
        }

        quads.add(
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
    organismCache.store.values.forEach {
        it.resolve(taxonomyDatabaseCache)
    }

    println(taxonomyDatabaseCache.store.values)
    println("Done")
}

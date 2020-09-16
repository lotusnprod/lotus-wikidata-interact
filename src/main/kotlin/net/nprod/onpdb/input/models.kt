package net.nprod.onpdb.input

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
    val title: String?,
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

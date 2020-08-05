package net.nprod.onpdb.goldcleaner

import net.nprod.onpdb.helpers.parseTSVFile
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File


object SourceDatabases : IntIdTable() {
    val name = varchar("name", 8)
}

class SourceDatabase(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SourceDatabase>(SourceDatabases)

    var name by SourceDatabases.name
}

object TaxoDbs : IntIdTable() {
    val name = varchar("name", 64)
}

class TaxoDb(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TaxoDb>(TaxoDbs)

    var name by TaxoDbs.name
}

object TaxRefs : IntIdTable() {
    val database = reference("taxodb", TaxoDbs)
    val dbId = varchar("dbid", 64)
}

class TaxRef(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TaxRef>(TaxRefs)

    var database by TaxoDb referencedOn TaxRefs.database
    var dbId by TaxRefs.dbId
}


object Organisms : IntIdTable() {
    val name = varchar("name", 64)
}

class Organism(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Organism>(Organisms)

    var name by Organisms.name
    val taxRefs by OrganismTaxRef referrersOn OrganismTaxRefs.organism
}


object OrganismTaxRefs : IntIdTable() {
    val organism = reference("organism", Organisms)
    val taxref = reference("taxref", TaxRefs)
}

class OrganismTaxRef(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<OrganismTaxRef>(OrganismTaxRefs)

    var organism by OrganismTaxRefs.organism
    var taxref by OrganismTaxRefs.taxref
}


object Compounds : IntIdTable() {
    var inchikey = varchar("inchikey", 32)
    var inchi = varchar("inchi", 4096)
    var smiles = varchar("smiles", 2048)
}

class Compound(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Compound>(Compounds)

    var inchikey by Compounds.inchikey
    var inchi by Compounds.inchi
    var smiles by Compounds.smiles
}

object References : IntIdTable() {
    val doi = varchar("doi", 128)
    val pmcid = varchar("pmcid", 16)
    val cleanedPmcId = varchar("cleanedpmcid", 16)
}

class Reference(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Reference>(References)

    val doi by References.doi
    val pmcid by References.pmcid
    val cleanedPmcId by References.cleanedPmcId
}

object Entries : IntIdTable() {
    val database = reference("database", SourceDatabases)
    val organism = reference("organism", Organisms)
    val compound = reference("compound", Compounds)
    val reference = reference("reference", References)
}

const val GOLD_PATH = "/home/bjo/Store/01_Research/opennaturalproductsdb/data/interim/tables/4_analysed/gold.tsv"

fun main() {
    Database.connect("jdbc:sqlite:data/test.sqlite", driver = "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(SourceDatabases)
        SchemaUtils.create(TaxoDbs)
        SchemaUtils.create(TaxRefs)
        SchemaUtils.create(Organisms)
        SchemaUtils.create(OrganismTaxRefs)
        SchemaUtils.create(Compounds)
        SchemaUtils.create(References)
        SchemaUtils.create(Entries)
    }

    println("Loading cache")
    print(" - databases")
    val databasesCache: MutableMap<String, SourceDatabase> = transaction {
        SourceDatabase.all().map {
            it.name to it
        }.toMap().toMutableMap()
    }
    println(" ${databasesCache.size} entries")

    print(" - taxo databases")
    val taxoDbCache: MutableMap<String, TaxoDb> = transaction {
        TaxoDb.all().map {
            it.name to it
        }.toMap().toMutableMap()
    }
    println(" ${taxoDbCache.size} entries")

    print(" - compounds")
    val compoundCache: MutableMap<String, Compound> = transaction {
        Compound.all().map {
            it.smiles to it
        }.toMap().toMutableMap()
    }
    println(" ${compoundCache.size} entries")

    // TODO
    val ReferenceCache: MutableMap<String, Reference> = mutableMapOf()
    val TaxRefCache: MutableMap<Pair<String, String>, TaxRef> = mutableMapOf()
    val OrganismCache: MutableMap<String, Organism> = mutableMapOf()

    println("Processing file")
    transaction {
        parseTSVFile(File(GOLD_PATH).bufferedReader())?.map {
            val db = it.getString("database")
            val dbObj = databasesCache[db] ?: {
                val dbObj = SourceDatabase.new {
                    name = db
                }
                databasesCache[db] = dbObj
            }()

            val taxoDb = it.getString("organismCleaned_dbTaxo")
            val taxoDbObj = taxoDbCache[taxoDb] ?: {
                val taxoDbObj  = TaxoDb.new {
                    name = taxoDb
                }
                taxoDbCache[taxoDb] = taxoDbObj
            }()

            val compound = it.getString("structureCleanedSmiles")
            val compoundObj = compoundCache[compound] ?: {
                val compoundObj = Compound.new {
                    smiles = it.getString("structureCleanedSmiles")
                    inchi = it.getString("structureCleanedInchi")
                    inchikey = it.getString("structureCleanedInchikey3D")
                }
                compoundCache[compound] = compoundObj
            }()
        }
    }
    println("Done")

}

/*fun findMaxSizes() {
    var setOfEntries: MutableSet<String>? = null
    val counts: MutableMap<String, Int> = mutableMapOf()
    parseTSVFile(File(GOLD_PATH).bufferedReader())?.map {
        setOfEntries ?: {
            setOfEntries = it.toFieldMap().keys
            counts.putAll(
                setOfEntries!!.map { it to 0 }
            )
        }
        it.toFieldMap().map {(key,value) ->
            val len = counts[key] ?: 0
            if (len<value.length) counts[key] = value.length
        }
    }
    println("Max sizes: $counts")
}*/
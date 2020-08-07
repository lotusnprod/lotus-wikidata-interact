package net.nprod.onpdb.goldcleaner

import net.nprod.onpdb.helpers.GZIPRead
import net.nprod.onpdb.helpers.parseTSVFile
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction


object SourceDatabases : IntIdTable() {
    var name = varchar("name", 8)
}

class SourceDatabase(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SourceDatabase>(SourceDatabases)

    var name by SourceDatabases.name
}

object TaxoDbs : IntIdTable() {
    var name = varchar("name", 64)
}

class TaxoDb(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TaxoDb>(TaxoDbs)

    var name by TaxoDbs.name
}

object TaxRefs : IntIdTable() {
    var database = reference("taxodb", TaxoDbs)
    var dbId = varchar("dbid", 64)
}

class TaxRef(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TaxRef>(TaxRefs)

    var database by TaxoDb referencedOn TaxRefs.database
    var dbId by TaxRefs.dbId
    var organism by Organism referencedOn OrganismTaxRefs.organism
}


object Organisms : IntIdTable() {
    var name = varchar("name", 64)
}

class Organism(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Organism>(Organisms)

    var name by Organisms.name
    val taxRefs by OrganismTaxRef referrersOn OrganismTaxRefs.taxref
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
    var doi = varchar("doi", 128)
    var pmcid = varchar("pmcid", 16)
    var pmid = varchar("pmid", 16)
}

class Reference(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Reference>(References)

    var doi by References.doi
    var pmcid by References.pmcid
    var pmid by References.pmid
}

object Entries : IntIdTable() {
    var database = reference("database", SourceDatabases)
    var organism = reference("organism", Organisms)
    var compound = reference("compound", Compounds)
    var reference = reference("reference", References)
}

class Entry(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Entry>(Entries)

    var database by Entries.database
    var organism by Entries.organism
    var compound by Entries.compound
    var reference by Entries.reference
}

const val GOLD_PATH = "/home/bjo/Store/01_Research/opennaturalproductsdb/data/interim/tables/4_analysed/gold.tsv.gz"

fun main() {
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
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

    print(" - references")
    val referenceCache: MutableMap<String, Reference> = transaction {
        Reference.all().map {
            it.doi to it
        }.toMap().toMutableMap()
    }
    println(" ${referenceCache.size} entries")

    print(" - taxref cache")
    val taxRefCache: MutableMap<Pair<String, String>, TaxRef> = transaction {
        TaxRef.all().map {
            Pair(it.database.name, it.dbId) to it
        }.toMap().toMutableMap()
    }
    println(" ${taxRefCache.size} entries")

    print(" - organisms")
    val organismCache: MutableMap<String, Organism> = transaction {
        Organism.all().map {
            it.name to it
        }.toMap().toMutableMap()
    }
    println(" ${organismCache.size} entries")
    println("Deleting the linking tables")
    transaction {
        Entries.deleteAll()
        OrganismTaxRefs.deleteAll()
    }
    println("Processing file")

    var count = 0

    val currentOrganism: Organism? = null
    parseTSVFile(GZIPRead(GOLD_PATH))?.map {
        println("Count: $count")
        count+=1
        transaction {
            val dbName = it.getString("database")
            val dbObj = databasesCache[dbName] ?: {
                val dbObj = SourceDatabase.new {
                    name = dbName
                }
                databasesCache[dbName] = dbObj
                dbObj
            }()

            val taxoDbName = it.getString("organismCleaned_dbTaxo")
            val taxoDbObj = taxoDbCache[taxoDbName] ?: {
                val taxoDbObj = TaxoDb.new {
                    name = taxoDbName
                }
                taxoDbCache[taxoDbName] = taxoDbObj
                taxoDbObj
            }()

            var compoundSmiles = it.getString("structureCleanedSmiles")
            val compoundObj = compoundCache[compoundSmiles] ?: {
                val compoundObj = Compound.new {
                    smiles = compoundSmiles
                    inchi = it.getString("structureCleanedInchi")
                    inchikey = it.getString("structureCleanedInchikey3D")
                }
                compoundCache[compoundSmiles] = compoundObj
                compoundObj
            }()

            val organismCleanedName = it.getString("organismCleaned")
            val organismObj = organismCache[organismCleanedName] ?: {
                val organismObj = Organism.new {
                    name = organismCleanedName
                }
                organismCache[organismCleanedName] = organismObj
                organismObj
            }()


            val orgDbID = it.getString("organismCleaned_dbTaxoTaxonId")
            val pairOrg = Pair(taxoDbName, orgDbID)
            val taxrefObj = taxRefCache[pairOrg] ?: {
                val taxRefObj = TaxRef.new {
                    this.database = taxoDbObj
                    this.dbId = orgDbID
                }
                taxRefCache[pairOrg] = taxRefObj
                taxRefObj
            }()

            var reference = it.getString("referenceCleanedDoi")
            val referenceObj = referenceCache[reference] ?: {
                val referenceObj = Reference.new {
                    this.doi = reference
                    this.pmcid = it.getString("referenceCleanedPmcid")
                    this.pmid = it.getString("referenceCleanedPmid")
                }
                referenceCache[reference] = referenceObj
                referenceObj
            }()

            OrganismTaxRef.new {
                this.organism = organismObj.id
                this.taxref = taxrefObj.id
            }

            Entry.new {
                this.database = dbObj.id
                this.organism = organismObj.id
                this.compound = compoundObj.id
                this.reference = referenceObj.id
            }
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
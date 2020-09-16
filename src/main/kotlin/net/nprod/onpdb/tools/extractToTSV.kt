package net.nprod.onpdb.tools

import com.univocity.parsers.tsv.TsvWriter
import com.univocity.parsers.tsv.TsvWriterSettings
import net.nprod.onpdb.input.loadData
import org.apache.logging.log4j.LogManager
import java.io.File

const val INHOUSE_PATH = "/home/bjo/Store/01_Research/opennaturalproductsdb/data/interim/tables/4_analysed/inhouseDbTriplets.tsv.gz"

fun main() {
    val logger = LogManager.getLogger("net.nprod.onpdb.tools.main")
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
            it.value.rankIds.map { taxid ->
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

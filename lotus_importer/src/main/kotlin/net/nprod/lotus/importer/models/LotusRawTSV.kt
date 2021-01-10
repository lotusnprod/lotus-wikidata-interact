/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.jobs

data class LotusRawTSV(
    val database: String,
    val organism: LotusRawOrganism,
    val compound: LotusRawCompound,
    val reference: LotusRawReference
) {
    companion object {
        fun fromRecord(record: com.univocity.parsers.common.record.Record): LotusRawTSV {
            val database = record.getString("database")
            val organismCleaned = record.getString("organismCleaned")
            val organismDb = record.getString("organismCleaned_dbTaxo")
            val organismID = record.getString("organismCleaned_id")
            val organismRanks = record.getString("organismCleaned_dbTaxoTaxonRanks")
            val organismNames = record.getString("organismCleaned_dbTaxoTaxonomy")

            val smiles = record.getString("structureCleanedSmiles")
            val inchi = record.getString("structureCleanedInchi")
            val inchiKey = record.getString("structureCleanedInchikey3D")
            val compoundName = record.getString("structureCleaned_nameTraditional")
            val iupacName = record.getString("structureCleaned_nameIupac")
            val unspecifiedStereocenters = record.getInt("structureCleaned_stereocenters_unspecified")
            val totalCenters = record.getInt("structureCleaned_stereocenters_total")

            val title = record.getString("referenceCleanedTitle")
            val pmcid = record.getString("referenceCleanedPmcid")
            val pmid = record.getString("referenceCleanedPmid")
            val doi = record.getString("referenceCleanedDoi")



            return LotusRawTSV(
                database = database,
                organism = LotusRawOrganism(organismCleaned, organismDb, organismID, organismRanks, organismNames),
                compound = LotusRawCompound(
                    smiles,
                    inchi,
                    inchiKey,
                    compoundName,
                    iupacName,
                    unspecifiedStereocenters,
                    totalCenters
                ),
                reference = LotusRawReference(title, pmcid, pmid, doi)
            )
        }
    }
}



data class LotusRawOrganism(
    val organismCleaned: String,
    val organismDb: String,
    val organismID: String,
    val organismRanks: String,
    val organismNames: String
)

data class LotusRawCompound(
    val smiles: String,
    val inchi: String,
    val inchiKey: String,
    val compoundName: String,
    val iupacName: String,
    val unspecifiedStereocenters: Int,
    val totalCenters: Int
)

data class LotusRawReference(val title: String, val pmcid: String, val pmid: String, val doi: String)

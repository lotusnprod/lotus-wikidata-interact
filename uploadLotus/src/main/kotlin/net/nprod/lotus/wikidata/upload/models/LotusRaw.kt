/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.jobs

data class LotusRaw(
    val organism: LotusRawOrganism,
    val compound: LotusRawCompound,
    val reference: LotusRawReference
) {
    companion object {
        fun fromRecord(record: com.univocity.parsers.common.record.Record): LotusRaw {
            val organismCleaned = record.getString("organismCleaned")
            val organismDb = record.getString("organismCleaned_dbTaxo")
            val organismID = record.getString("organismCleaned_id")
            val organismRanks = record.getString("organismCleaned_dbTaxoTaxonRanks")
            val organismNames = record.getString("organismCleaned_dbTaxoTaxonomy")

            val smiles = record.getString("structureCleanedSmiles")
            val inchi = record.getString("structureCleanedInchi")
            val inchiKey = record.getString("structureCleanedInchikey")
            val compoundName = record.getString("structureCleaned_nameTraditional")
            val iupacName = record.getString("structureCleaned_nameIupac")
            val unspecifiedStereocenters = record.getInt("structureCleaned_stereocenters_unspecified")
            val totalCenters = record.getInt("structureCleaned_stereocenters_total")

            val title = record.getString("referenceCleanedTitle")
            val pmcid = record.getString("referenceCleanedPmcid")
            val pmid = record.getString("referenceCleanedPmid")
            val doi = record.getString("referenceCleanedDoi")

            return LotusRaw(
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

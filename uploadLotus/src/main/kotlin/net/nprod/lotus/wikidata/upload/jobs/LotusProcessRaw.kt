/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.jobs

import net.nprod.lotus.helpers.ifEqualReplace
import net.nprod.lotus.helpers.titleCleaner
import net.nprod.lotus.wikidata.upload.input.Compound
import net.nprod.lotus.wikidata.upload.input.DataTotal
import net.nprod.lotus.wikidata.upload.input.Organism
import net.nprod.lotus.wikidata.upload.input.Reference
import net.nprod.lotus.wikidata.upload.input.Triplet
import net.nprod.lotus.wikidata.upload.oldprocessor.InvalidEntryDataException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val TaxonomyDatabaseExclusionList = listOf("IPNI", "IRMNG (old)")
val RequiredTaxonRanks = listOf("variety", "genus", "subgenus", "species", "subspecies", "family")

val InChIKeyRegexp: Regex = "[A-Z]{14}-[A-Z]{10}-[A-Z]".toRegex()

private fun String.validateInChIKey(): String {
    if (!this.matches(InChIKeyRegexp)) throw InvalidEntryDataException("InChIKey $this invalid")
    return this
}

interface ItemProcessor<T, S> {
    fun process(items: Iterable<T>): S
}

class LotusProcessRaw : ItemProcessor<LotusRaw, DataTotal> {
    private var logger: Logger = LoggerFactory.getLogger(LotusProcessRaw::class.java)

    override fun process(items: Iterable<LotusRaw>): DataTotal {
        val dataTotal = DataTotal()

        items.filter(::canBeProcessed).forEach { lotusRaw ->
            logger.error("Invalid entry: $lotusRaw")
        }

        items.filter(::canBeProcessed).forEach { lotusRaw ->
            val organismObj =
                with(lotusRaw.organism) {
                    dataTotal.organismCache
                        .getOrNew(organismCleaned) {
                            Organism(name = organismCleaned)
                        }.apply {
                            finalIds[organismDb] = organismID
                            textRanks[organismDb] = organismRanks
                            textNames[organismDb] = organismNames
                        }
                }

            val inchiKey = lotusRaw.compound.inchiKey.validateInChIKey()

            processEntry(lotusRaw, dataTotal, inchiKey, organismObj)
        }

        logger.info("Resolving the taxo DB")
        dataTotal.organismCache.store.values
            .forEach { it.resolve(dataTotal.taxonomyDatabaseCache) }
        return dataTotal
    }

    private fun canBeProcessed(lotusRaw: LotusRaw) =
        RequiredTaxonRanks.any { lotusRaw.organism.organismRanks.contains("it") } ||
            lotusRaw.organism.organismDb !in TaxonomyDatabaseExclusionList

    private fun processEntry(
        lotusRaw: LotusRaw,
        dataTotal: DataTotal,
        inchiKey: String,
        organismObj: Organism,
    ) {
        try {
            val compoundObj =
                with(lotusRaw.compound) {
                    dataTotal.compoundCache.getOrNew(smiles) {
                        Compound(
                            name = compoundName,
                            smiles = smiles,
                            inchi = inchi,
                            inchikey = inchiKey,
                            iupac = iupacName,
                            unspecifiedStereocenters = unspecifiedStereocenters,
                            atLeastSomeStereoDefined = unspecifiedStereocenters != totalCenters,
                        )
                    }
                }

            val referenceObj =
                with(lotusRaw.reference) {
                    dataTotal.referenceCache.getOrNew(doi) {
                        val title = title.titleCleaner()
                        Reference(
                            doi = doi,
                            title = title,
                            pmcid = pmcid?.ifEqualReplace("NA", "") ?: "",
                            pmid = pmid?.ifEqualReplace("NA", "") ?: "",
                        )
                    }
                }

            /**
             * We had a bug where we matched all the NA to a single article
             */
            if (referenceObj.doi != "NA") {
                dataTotal.triplets.add(Triplet(organismObj, compoundObj, referenceObj))
            }
        } catch (e: InvalidEntryDataException) {
            logger.error("Invalid Entry Data: ${e.message}")
        }
    }
}

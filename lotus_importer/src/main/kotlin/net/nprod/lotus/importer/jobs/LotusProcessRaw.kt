/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.jobs

import net.nprod.lotus.importer.input.Compound
import net.nprod.lotus.importer.input.DataTotal
import net.nprod.lotus.importer.input.Database
import net.nprod.lotus.importer.input.Organism
import net.nprod.lotus.importer.input.Quad
import net.nprod.lotus.importer.input.Reference
import net.nprod.lotus.importer.input.ifEqualReplace
import net.nprod.lotus.importer.oldprocessor.InvalidEntryDataException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ItemProcessor

val InChIKeyRegexp: Regex = "[A-Z]{14}-[A-Z]{10}-[A-Z]".toRegex()
private fun String.validateInChIKey(): String {
    if (!this.matches(InChIKeyRegexp)) throw InvalidEntryDataException("InChIKey $this invalid")
    return this
}

class LotusProcessRaw : ItemProcessor<List<LotusRawTSV>, DataTotal> {
    private var parameters: JobParameters? = null
    private var logger: Logger = LoggerFactory.getLogger(LotusProcessRaw::class.java)

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        parameters = stepExecution.jobParameters
    }

    override fun process(items: List<LotusRawTSV>): DataTotal {
        val dataTotal = DataTotal()
        items.forEach { lotusRaw ->
            if (RequiredTaxonRanks.any { lotusRaw.organism.organismRanks.contains("it") } ||
                lotusRaw.organism.organismDb !in TaxonomyDatabaseExclusionList
            ) {
                val databaseObj =
                    dataTotal.databaseCache.getOrNew(lotusRaw.database) { Database(name = lotusRaw.database) }

                val organismObj = with(lotusRaw.organism) {
                    dataTotal.organismCache.getOrNew(organismCleaned) {
                        Organism(name = organismCleaned)
                    }.apply {
                        finalIds[organismDb] = organismID
                        textRanks[organismDb] = organismRanks
                        textNames[organismDb] = organismNames
                    }
                }

                val inchiKey = lotusRaw.compound.inchiKey.validateInChIKey()

                try {
                    val compoundObj = with(lotusRaw.compound) {
                        dataTotal.compoundCache.getOrNew(smiles) {
                            Compound(
                                name = compoundName,
                                smiles = smiles,
                                inchi = inchi,
                                inchikey = inchiKey,
                                iupac = iupacName,
                                unspecifiedStereocenters = unspecifiedStereocenters,
                                atLeastSomeStereoDefined = unspecifiedStereocenters != totalCenters
                            )
                        }
                    }

                    val referenceObj = with(lotusRaw.reference) {
                        dataTotal.referenceCache.getOrNew(doi) {
                            Reference(
                                doi = doi,
                                title = title.ifEqualReplace("NA", ""),
                                pmcid = pmcid.ifEqualReplace("NA", ""),
                                pmid = pmid.ifEqualReplace("NA", "")
                            )
                        }
                    }

                    dataTotal.quads.add(Quad(databaseObj, organismObj, compoundObj, referenceObj))
                } catch (e: InvalidEntryDataException) {
                    logger.error(e.toString())
                    throw RuntimeException("It works")
                }

            } else {
                logger.error("Invalid entry: $lotusRaw")
            }
        }

        logger.info("Resolving the taxo DB")
        dataTotal.organismCache.store.values.forEach { it.resolve(dataTotal.taxonomyDatabaseCache) }
        return dataTotal
    }
}

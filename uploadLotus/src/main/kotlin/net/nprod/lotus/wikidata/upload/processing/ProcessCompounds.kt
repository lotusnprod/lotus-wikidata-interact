/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.processing

import io.ktor.util.KtorExperimentalAPI
import net.nprod.lotus.chemistry.smilesToCanonical
import net.nprod.lotus.chemistry.smilesToFormula
import net.nprod.lotus.chemistry.subscriptFormula
import net.nprod.lotus.wikidata.upload.input.DataTotal
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.models.entries.WDCompound
import net.nprod.lotus.wdimport.wd.publishing.IPublisher
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import org.apache.logging.log4j.LogManager
import org.openscience.cdk.exception.InvalidSmilesException
import kotlin.time.ExperimentalTime

/** Labels are limited to 250 on WikiData **/
const val MAXIMUM_COMPOUND_NAME_LENGTH: Int = 250

/** InChIs are limited to 1500 on WikiData **/
const val MAXIMUM_INCHI_LENGTH: Int = 1500

/**
 * Take each compound entry and attempt to add it into WikiData.
 * It is calling a lot of functions with serious side effects: organismProcessor and referenceProcessor are major
 * they are doing the same thing this function is doing but to create taxa and articles/references
 */
@ExperimentalTime
@KtorExperimentalAPI
fun DataTotal.processCompounds(
    wdFinder: WDFinder,
    instanceItems: InstanceItems,
    wikidataCompoundCache: MutableMap<InChIKey, String>,
    publisher: IPublisher
) {
    val logger = LogManager.getLogger("net.nprod.lotus.wdimport.processing.processCompounds")

    val taxonProcessor = TaxonProcessor(this, publisher, wdFinder, instanceItems)
    val referenceProcessor = ReferenceProcessor(this, publisher, wdFinder, instanceItems)

    val count = this.compoundCache.store.size
    this.compoundCache.store.values.forEachIndexed { idx, compound ->
        logger.info("Compound with name ${compound.name} $idx/$count")
        val compoundName = if (compound.name.length < MAXIMUM_COMPOUND_NAME_LENGTH) compound.name else compound.inchikey
        val isomericSMILES = if (compound.atLeastSomeStereoDefined) compound.smiles else null
        val smiles = compound.smiles.replace("\n", "")
        val wdcompound = WDCompound(
            label = compoundName,
            inChIKey = compound.inchikey,
            inChI = if (compound.inchi.length < MAXIMUM_INCHI_LENGTH) compound.inchi else null,
            isomericSMILES = isomericSMILES,
            canonicalSMILES = try {
                smilesToCanonical(smiles)
            } catch (e: InvalidSmilesException) {
                logger.error("Invalid smiles exception: ${e.message}")
                return@forEachIndexed
            },
            chemicalFormula = try {
                subscriptFormula(smilesToFormula(smiles))
            } catch (e: InvalidSmilesException) {
                logger.error("Invalid smiles exception cannot make a formula: ${e.message}")
                return@forEachIndexed
            },
            iupac = compound.iupac,
            undefinedStereocenters = compound.unspecifiedStereocenters
        ).tryToFind(wdFinder, instanceItems, wikidataCompoundCache)

        logger.info(wdcompound)

        wdcompound.apply {
            this@processCompounds.triplets.filter { it.compound == compound }.distinct().groupBy { it.organism }
                .forEach { (organism, quads) ->
                    try {
                        val taxon = taxonProcessor.get(organism)
                        logger.info(" Found taxon $taxon")
                        taxon?.let {
                            foundInTaxon(
                                taxon
                            ) {
                                quads.map {
                                    statedIn(referenceProcessor.get(it.reference).id)
                                }
                            }
                        }
                    } catch (e: InvalidTaxonName) {
                        logger.error(" ERROR: Couldn't a good database for the organism: ${organism.name} - ${e.message}")
                    }
                }
        }
        publisher.publish(wdcompound, "upserting compound")
    }
}


/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package processing

import input.DataTotal
import io.ktor.util.KtorExperimentalAPI
import net.nprod.lotus.chemistry.smilesToCanonical
import net.nprod.lotus.chemistry.smilesToFormula
import net.nprod.lotus.chemistry.subscriptFormula
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.models.entries.WDCompound
import net.nprod.lotus.wdimport.wd.publishing.IPublisher
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import org.apache.logging.log4j.LogManager

/** Labels are limited to 250 on WikiData **/
const val MAXIMUM_COMPOUND_NAME_LENGTH: Int = 250

/** InChIs are limited to 1500 on WikiData **/
const val MAXIMUM_INCHI_LENGTH: Int = 1500

/**
 * Take each compound entry and attempt to add it into WikiData.
 * It is calling a lot of functions with serious side effects: organismProcessor and referenceProcessor are major
 * they are doing the same thing this function is doing but to create taxa and articles/references
 */
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
        val wdcompound = WDCompound(
            label = compoundName,
            inChIKey = compound.inchikey,
            inChI = if (compound.inchi.length < MAXIMUM_INCHI_LENGTH) compound.inchi else null,
            isomericSMILES = isomericSMILES,
            canonicalSMILES = smilesToCanonical(compound.smiles),
            chemicalFormula = subscriptFormula(smilesToFormula(compound.smiles)),
            iupac = compound.iupac,
            undefinedStereocenters = compound.unspecifiedStereocenters
        ).tryToFind(wdFinder, instanceItems, wikidataCompoundCache)

        logger.info(wdcompound)

        wdcompound.apply {
            this@processCompounds.quads.filter { it.compound == compound }.distinct().forEach { quad ->
                logger.info("Ok lets go for a quad: $quad")
                val organism = taxonProcessor.get(quad.organism)
                logger.info(" Found organism $organism")

                foundInTaxon(
                    organism
                ) {
                    statedIn(referenceProcessor.get(quad.reference).id)
                }
            }
        }
        publisher.publish(wdcompound, "upserting compound")
    }
}

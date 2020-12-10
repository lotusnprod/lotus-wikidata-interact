package net.nprod.lotus.wdimport

import net.nprod.lotus.chemistry.smilesToCanonical
import net.nprod.lotus.chemistry.smilesToFormula
import net.nprod.lotus.chemistry.subscriptFormula
import net.nprod.lotus.input.DataTotal
import net.nprod.lotus.input.Organism
import net.nprod.lotus.input.Reference
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.interfaces.Publisher
import net.nprod.lotus.wdimport.wd.models.WDArticle
import net.nprod.lotus.wdimport.wd.models.WDCompound
import net.nprod.lotus.wdimport.wd.models.WDTaxon
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import org.apache.logging.log4j.Logger

fun processCompounds(
    dataTotal: DataTotal,
    logger: Logger,
    wdFinder: WDFinder,
    instanceItems: InstanceItems,
    wikidataCompoundCache: MutableMap<InChIKey, String>,
    organisms: Map<Organism, WDTaxon>,
    references: Map<Reference, WDArticle>,
    publisher: Publisher
) {
    dataTotal.compoundCache.store.forEach { (_, compound) ->
        logger.info("Compound with name ${compound.name}")
        val compoundName = if (compound.name.length < 250) compound.name else compound.inchikey
        val isomericSMILES = if (compound.atLeastSomeStereoDefined) compound.smiles else null
        val wdcompound = WDCompound(
            name = compoundName,
            inChIKey = compound.inchikey,
            inChI = compound.inchi,
            isomericSMILES = isomericSMILES,
            canonicalSMILES = smilesToCanonical(compound.smiles),
            chemicalFormula = subscriptFormula(smilesToFormula(compound.smiles)),
            iupac = compound.iupac,
            undefinedStereocenters = compound.unspecifiedStereocenters
        ).tryToFind(wdFinder, instanceItems, wikidataCompoundCache)
        logger.info(wdcompound)
        wdcompound.apply {
            dataTotal.quads.filter { it.compound == compound }.distinct().forEach { quad ->
                val organism = organisms[quad.organism]
                organism?.let {
                    foundInTaxon(
                        organism
                    ) {
                        statedIn(
                            references[quad.reference]?.id
                                ?: throw Exception("That's bad we talk about a reference we don't have.")
                        )
                    }
                }
            }
        }
        publisher.publish(wdcompound, "upserting compound")
    }
}
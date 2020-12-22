package net.nprod.lotus.wdimport

import net.nprod.lotus.chemistry.smilesToCanonical
import net.nprod.lotus.chemistry.smilesToFormula
import net.nprod.lotus.chemistry.subscriptFormula
import net.nprod.lotus.input.DataTotal
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.interfaces.Publisher
import net.nprod.lotus.wdimport.wd.models.WDCompound
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import org.apache.logging.log4j.Logger

fun processCompounds(
    dataTotal: DataTotal,
    logger: Logger,
    wdFinder: WDFinder,
    instanceItems: InstanceItems,
    wikidataCompoundCache: MutableMap<InChIKey, String>,
    organisms: OrganismProcessor,
    references: ReferencesProcessor,
    publisher: Publisher
) {
    val count = dataTotal.compoundCache.store.size
    dataTotal.compoundCache.store.values.forEachIndexed { idx, compound ->
        logger.info("Compound with name ${compound.name} $idx/$count")
        val compoundName = if (compound.name.length < 250) compound.name else compound.inchikey
        val isomericSMILES = if (compound.atLeastSomeStereoDefined) compound.smiles else null
        val wdcompound = WDCompound(
            name = compoundName,
            inChIKey = compound.inchikey,
            inChI = if (compound.inchi.length < 1500) compound.inchi else null, // InChIs are limited to 1500 on WikiData
            isomericSMILES = isomericSMILES,
            canonicalSMILES = smilesToCanonical(compound.smiles),
            chemicalFormula = subscriptFormula(smilesToFormula(compound.smiles)),
            iupac = compound.iupac,
            undefinedStereocenters = compound.unspecifiedStereocenters
        ).tryToFind(wdFinder, instanceItems, wikidataCompoundCache)
        logger.info(wdcompound)
        wdcompound.apply {
            dataTotal.quads.filter { it.compound == compound }.distinct().forEach { quad ->
                logger.info("Ok lets go for a quad: $quad")
                val organism = organisms.get(quad.organism)
                logger.info(" Found organism $organism")

                foundInTaxon(
                    organism
                ) {
                    statedIn(references.get(quad.reference).id)
                }

            }
        }
        publisher.publish(wdcompound, "upserting compound")
    }
}
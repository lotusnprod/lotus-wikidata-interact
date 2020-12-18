package net.nprod.lotus.wdimport

import net.nprod.lotus.input.DataTotal
import net.nprod.lotus.input.Organism
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.interfaces.Publisher
import net.nprod.lotus.wdimport.wd.models.WDTaxon
import org.apache.logging.log4j.LogManager

class InvalidTaxonName : RuntimeException()


class OrganismProcessor(
    val dataTotal: DataTotal, val publisher: Publisher, val wdFinder: WDFinder,
    val instanceItems: InstanceItems,
) {
    val logger = LogManager.getLogger(OrganismProcessor::class.qualifiedName)
    val organismCache: MutableMap<Organism, WDTaxon> = mutableMapOf()
    fun taxonFromOrganism(organism: Organism): WDTaxon {
        logger.debug("Organism Ranks and Ids: ${organism.rankIds}")

        var taxon: WDTaxon? = null

        listOf(
            "ITIS",
            "GBIF",
            "NCBI",
            "Index Fungorum",
            "The Interim Register of Marine and Nonmarine Genera",
            "World Register of Marine Species",
            "Database of Vascular Plants of Canada (VASCAN)",
            "GBIF Backbone Taxonomy"
        ).forEach { taxonDbName ->
            // First we check if we have that db in the current organism
            val taxonDb = organism.rankIds.keys.firstOrNull { it.name == taxonDbName }
            if (taxon != null) return@forEach
            taxonDb?.let {
                val family = organism.rankIds[taxonDb]?.firstOrNull { it.first == "family" }?.second?.name
                val genus = organism.rankIds[taxonDb]?.firstOrNull { it.first == "genus" }?.second?.name
                val species = organism.rankIds[taxonDb]?.firstOrNull { it.first == "species" }?.second?.name

                // This is a ugly hack, we need to find a way to get a proper taxonomy input
                // that take every rank into account.

                // We make sure neither genus and species are empty, as we cannot push empty properties to WikiData
                if (genus == "" || species == "") {
                    logger.error("A taxon name was empty using the database $taxonDbName (null is ok): family=[$family] genus=[$genus] species=[$species]")
                    logger.error(organism.prettyPrint())
                    throw InvalidTaxonName()
                }

                val familyWD = if (family != null && family != "") {
                    WDTaxon(
                        name = family,
                        parentTaxon = null,
                        taxonName = family,
                        taxonRank = InstanceItems::family
                    ).tryToFind(wdFinder, instanceItems)
                } else {
                    null
                }

                taxon = familyWD

                taxon?.let { if (!it.published) publisher.publish(it, "Created a missing taxon") }

                val genusWD = if (genus != null) {
                    WDTaxon(
                        name = genus,
                        parentTaxon = familyWD?.id,
                        taxonName = genus,
                        taxonRank = InstanceItems::genus
                    ).tryToFind(wdFinder, instanceItems)
                } else {
                    null
                }

                taxon = genusWD
                taxon?.let { if (!it.published) publisher.publish(it, "Created a missing genus") }

                if (species != null) {
                    val speciesWD = WDTaxon(
                        name = species,
                        parentTaxon = genusWD?.id,
                        taxonName = species,
                        taxonRank = InstanceItems::species
                    ).tryToFind(wdFinder, instanceItems)
                    taxon = speciesWD

                }
            }
        }

        val finalTaxon = taxon
        if (finalTaxon == null) {
            logger.error("This is pretty bad. Here is what I know about an organism that failed: $organism")
            throw Exception("Sorry we couldn't find any info from the accepted reference taxonomy source, we only have: ${organism.rankIds.keys.map { it.name }}")
        } else {
            organism.textIds.forEach { dbEntry ->
                finalTaxon.addTaxoDB(dbEntry.key, dbEntry.value.split("|").last())
            }

            if (!finalTaxon.published) publisher.publish(finalTaxon, "Created a missing taxon")
            return finalTaxon
        }
    }

    fun get(key: Organism): WDTaxon {
        return organismCache.getOrPut(key) {
            taxonFromOrganism(key)
        }
    }
}

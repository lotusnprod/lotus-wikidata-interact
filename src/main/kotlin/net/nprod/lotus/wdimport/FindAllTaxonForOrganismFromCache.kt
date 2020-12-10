package net.nprod.lotus.wdimport

import net.nprod.lotus.input.DataTotal
import net.nprod.lotus.input.Organism
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.interfaces.Publisher
import net.nprod.lotus.wdimport.wd.models.WDTaxon
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import org.apache.logging.log4j.LogManager

class InvalidTaxonName: RuntimeException()

fun processOrganisms(
    dataTotal: DataTotal,
    wdSparql: ISparql,
    wdFinder: WDFinder,
    instanceItems: InstanceItems,
    publisher: Publisher
): Map<Organism, WDTaxon> {
    val logger = LogManager.getLogger("findAllTAxonForOrganismFromCache")
    return dataTotal.organismCache.store.values.mapNotNull { organism ->

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

                taxon?.let { if(!it.published) publisher.publish(it, "Created a missing taxon") }
                if (genus != null) {
                    val genusWD = WDTaxon(
                        name = genus,
                        parentTaxon = familyWD?.id,
                        taxonName = genus,
                        taxonRank = InstanceItems::genus
                    ).tryToFind(wdFinder, instanceItems)

                    taxon = genusWD
                    if (species != null) {
                        if(!genusWD.published) publisher.publish(genusWD, "Created a missing genus")

                        val speciesWD = WDTaxon(
                            name = species,
                            parentTaxon = genusWD.id,
                            taxonName = species,
                            taxonRank = InstanceItems::species
                        ).tryToFind(wdFinder, instanceItems)
                        taxon = speciesWD
                    }
                }
            }
        }

        if (taxon == null) {
            logger.error("This is pretty bad here is what I know about an organism that failed: $organism")
            throw Exception("Sorry we couldn't find any info from the accepted reference taxonomy source, we only have: ${organism.rankIds.keys.map { it.name }}")
        }

        taxon?.let {
            organism.textIds.forEach { dbEntry ->
                it.addTaxoDB(dbEntry.key, dbEntry.value.split("|").last())
            }

            publisher.publish(it, "Created a missing taxon")
            organism to it
        }

    }.toMap()
}
package net.nprod.lotus.wdimport

import net.nprod.lotus.input.DataTotal
import net.nprod.lotus.input.Organism
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.interfaces.Publisher
import net.nprod.lotus.wdimport.wd.models.WDTaxon
import org.apache.logging.log4j.LogManager
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import kotlin.reflect.KProperty1

class InvalidTaxonName : RuntimeException()

data class AcceptedTaxonEntry(
    val rank: String,
    val instanceItem: KProperty1<InstanceItems, ItemIdValue>,
    val value: String
)

class OrganismProcessor(
    val dataTotal: DataTotal, val publisher: Publisher, val wdFinder: WDFinder,
    val instanceItems: InstanceItems,
) {
    val logger = LogManager.getLogger(OrganismProcessor::class.qualifiedName)
    val organismCache: MutableMap<Organism, WDTaxon> = mutableMapOf()
    fun taxonFromOrganism(organism: Organism): WDTaxon {
        logger.debug("Organism Ranks and Ids: ${organism.rankIds}")

        var taxon: WDTaxon? = null

        val acceptedRanks = mutableListOf<AcceptedTaxonEntry>()
        var fullTaxonFound = false

        // We are going to go over this list of DBs, by order of trust and check if we have taxon info in them
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
            if (fullTaxonFound) return@forEach
            val taxonDb = organism.rankIds.keys.firstOrNull { it.name == taxonDbName }
            acceptedRanks.clear()
            taxonDb?.let {
                val ranks = listOf(
                    "family" to InstanceItems::family,
                    "subfamily" to InstanceItems::subfamily,
                    "tribe" to InstanceItems::tribe,
                    "subtribe" to InstanceItems::subtribe,
                    "genus" to InstanceItems::genus,
                    "species" to InstanceItems::species,
                    "variety" to InstanceItems::variety
                )
                var lowerTaxonIdFound = false

                ranks.forEach { (rankName, rankItem) ->
                    val entity = organism.rankIds[taxonDb]?.firstOrNull { it.first.toLowerCase() == rankName }?.second?.name
                    if (!entity.isNullOrEmpty()) {
                        acceptedRanks.add(AcceptedTaxonEntry(rankName, rankItem, entity))
                        if (rankName in listOf("genus", "species", "variety", "family")) {
                            lowerTaxonIdFound = true
                            fullTaxonFound = true
                        }
                    }
                }

                if (!lowerTaxonIdFound) {
                    logger.error("A taxon name was empty using the database $taxonDbName (null is ok): ${organism.rankIds[taxonDb]}")
                    logger.error(organism.prettyPrint())
                    throw InvalidTaxonName()
                }
            }
        }

        // If we have no entry we would have exited already with a InvalidTaxonName exception

        acceptedRanks.forEach {
            taxon?.let { if (!it.published) publisher.publish(it, "Created a missing taxon") }
            val tax = WDTaxon(
                name = it.value,
                parentTaxon = taxon?.id,
                taxonName = it.value,
                taxonRank = it.instanceItem
            ).tryToFind(wdFinder, instanceItems)
            taxon = tax
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

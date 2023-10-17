/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.processing

import net.nprod.lotus.taxa.fixSpecies
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.models.entries.WDTaxon
import net.nprod.lotus.wdimport.wd.publishing.IPublisher
import net.nprod.lotus.wikidata.upload.input.Organism
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import java.util.Locale
import kotlin.reflect.KProperty1

/**
 * We got a wrong taxon name
 */
class InvalidTaxonName : RuntimeException()

/**
 * We got a wrong taxon name
 */
class NotEnoughInfoAboutTaxonException(override val message: String) : RuntimeException()

data class AcceptedTaxonEntry(
    val rank: String,
    val instanceItem: KProperty1<InstanceItems, ItemIdValue>,
    val value: String,
)

/**
 * These are the taxonomic DBs we are using as references
 */
val LIST_OF_ACCEPTED_DBS: Array<String> =
    arrayOf(
        "Open Tree of Life",
        "ITIS",
        "GBIF",
        "NCBI",
        "Index Fungorum",
        "The Interim Register of Marine and Nonmarine Genera",
        "World Register of Marine Species",
        "Database of Vascular Plants of Canada (VASCAN)",
        "iNaturalist",
        "Catalogue of Life",
        "World Flora Online",
        "VASCAN",
        "GBIF Backbone Taxonomy",
        "AlgaeBase",
        "BirdLife International",
        "Mammal Species of the World",
        "EUNIS",
    )

/**
 * Process the Taxa, it will take the best database and use this taxon for the given entries.
 *
 */
class TaxonProcessor(
    val publisher: IPublisher,
    val wdFinder: WDFinder,
    val instanceItems: InstanceItems,
) {
    val logger: Logger = LogManager.getLogger(TaxonProcessor::class.qualifiedName)
    val createNew = System.getenv("CREATE_SPECIES") == "yes_and_I_understand_that_I_will_not_complain_if_things_are_wrong"
    val organismCache: MutableMap<Organism, WDTaxon?> = mutableMapOf()

    fun taxonFromOrganism(organism: Organism): WDTaxon? {
        logger.debug("Organism Ranks and Ids: ${organism.rankIds}")

        // We are going to go over this list of DBs, by order of trust and check if we have taxon info in them
        val acceptedRanks = searchForTaxonInfo(organism)

        // If we have no entry we would have exited already with a InvalidTaxonName exception
        var taxon: WDTaxon? = null
        acceptedRanks.reversed().forEach {
            taxon?.let { return@forEach } // We skip all if we found one
            val name = fixSpecies(it.value)
            val tax =
                WDTaxon(
                    label = name,
                    parentTaxon = taxon?.id,
                    taxonName = name,
                    taxonRank = it.instanceItem,
                ).tryToFind(wdFinder, instanceItems)
            taxon = tax
        }

        taxon?.let {
            if (!it.published) {
                if (createNew) {
                    publisher.publish(it, "Created a missing taxon")
                } else {
                    return null
                }
            }
        }

        val finalTaxon = taxon
        if (finalTaxon == null) {
            logger.error("This is pretty bad. Here is what I know about an organism that failed: $organism")
            throw NotEnoughInfoAboutTaxonException(
                """
                Sorry we couldn't find any info from the accepted reference taxonomy source,
                | we only have: ${organism.rankIds.keys.map { it.name }}
                """.trimMargin(),
            )
        } else {
            organism.finalIds.forEach { dbEntry -> finalTaxon.addTaxoDB(dbEntry.key, dbEntry.value) }

            if (!finalTaxon.published) publisher.publish(finalTaxon, "Created a missing taxon")
            return finalTaxon
        }
    }

    @Suppress("NestedBlockDepth")
    private fun searchForTaxonInfo(organism: Organism): MutableList<AcceptedTaxonEntry> {
        val acceptedRanks = mutableListOf<AcceptedTaxonEntry>()
        var fullTaxonFound = false
        LIST_OF_ACCEPTED_DBS.forEach { taxonDbName ->
            // First we check if we have that db in the current organism
            if (fullTaxonFound) return@forEach

            val taxonDb =
                organism.rankIds.keys.firstOrNull { it.name == taxonDbName }
                    ?: RuntimeException("Taxonomy database not found, but we have a reference to it")

            acceptedRanks.clear()

            val ranks =
                listOf(
                    "family" to InstanceItems::family,
                    "subfamily" to InstanceItems::subfamily,
                    "tribe" to InstanceItems::tribe,
                    "subtribe" to InstanceItems::subtribe,
                    "genus" to InstanceItems::genus,
                    "species" to InstanceItems::species,
                    "variety" to InstanceItems::variety,
                )

            ranks.forEach { (rankName, rankItem) ->
                val entity =
                    organism.rankIds[taxonDb]?.firstOrNull { it.first.lowercase(Locale.getDefault()) == rankName }?.second?.name
                if (!entity.isNullOrEmpty()) {
                    acceptedRanks.add(AcceptedTaxonEntry(rankName, rankItem, entity))
                    if (rankName in listOf("genus", "subgenus", "subspecies", "species", "variety", "family")) {
                        fullTaxonFound = true
                    }
                }
            }
        }
        if (!fullTaxonFound) {
            logger.error("Taxon name empty using all of our known DBs: $organism")
            logger.error(acceptedRanks)
            logger.error(organism.prettyPrint())
            throw InvalidTaxonName()
        }
        return acceptedRanks
    }

    /**
     * Get or create a Wikidata taxon entry from an organism
     *
     * @throws InvalidTaxonName when the taxon cannot be found in any database
     */
    fun get(key: Organism): WDTaxon? = organismCache.getOrPut(key) { taxonFromOrganism(key) }
}

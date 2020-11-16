package net.nprod.lotus.wdimport

import net.nprod.lotus.input.DataTotal
import net.nprod.lotus.input.Organism
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.interfaces.Publisher
import net.nprod.lotus.wdimport.wd.models.WDTaxon
import net.nprod.lotus.wdimport.wd.query.WDKT
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import org.apache.logging.log4j.LogManager

fun findAllTaxonForOrganismFromCache(
    dataTotal: DataTotal,
    wdSparql: ISparql,
    instanceItems: InstanceItems,
    publisher: Publisher
): Map<Organism, WDTaxon> {
    val logger = LogManager.getLogger("findAllTAxonForOrganismFromCache")
    return dataTotal.organismCache.store.values.mapNotNull { organism ->

        logger.debug("Organism Ranks and Ids: ${organism.rankIds}")
        val wdFinder = WDFinder(WDKT(), wdSparql)
        var taxon: WDTaxon? = null

        listOf(
            "GBIF",
            "NCBI",
            "ITIS",
            "Index Fungorum",
            "The Interim Register of Marine and Nonmarine Genera",
            "World Register of Marine Species",
            "Database of Vascular Plants of Canada (VASCAN)",
            "GBIF Backbone Taxonomy"
        ).forEach { taxonDbName ->
            val taxonDb = organism.rankIds.keys.firstOrNull { it.name == taxonDbName }
            if (taxon != null) return@forEach
            taxonDb?.let {
                val genus = organism.rankIds[taxonDb]?.firstOrNull { it.first == "genus" }?.second?.name
                val species = organism.rankIds[taxonDb]?.firstOrNull { it.first == "species" }?.second?.name

                if (genus != null) {

                    val genusWD = WDTaxon(
                        name = genus,
                        parentTaxon = null,
                        taxonName = genus,
                        taxonRank = InstanceItems::genus
                    ).tryToFind(wdFinder, instanceItems)

                    taxon = genusWD
                    if (species != null) {
                        publisher.publish(genusWD, "Created a missing genus")

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
            throw Exception("Sorry we couldn't find any info from the accepted reference taxonomy source, we only have: ${organism.rankIds.keys.map { it.name }}")
        }

        taxon?.let {
            // TODO get that to work
            organism.textIds.forEach { dbEntry ->
                it.addTaxoDB(dbEntry.key, dbEntry.value.split("|").last())
            }

            publisher.publish(it, "Created a missing taxon")
            organism to it
        }

    }.toMap()
}
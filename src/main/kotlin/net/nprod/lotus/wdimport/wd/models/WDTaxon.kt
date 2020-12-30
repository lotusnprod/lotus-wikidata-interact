/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.publishing.Publishable
import net.nprod.lotus.wdimport.wd.publishing.RemoteItem
import net.nprod.lotus.wdimport.wd.publishing.RemoteProperty
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import kotlin.reflect.KProperty1

/**
 * This is used to convert the names from LOTUS to WikiData DB names
 */
val taxDBToProperty: Map<String, RemoteProperty?> = mapOf(
    "AmphibiaWeb" to InstanceItems::idAmphibiaTaxonomy,
    "ARKive" to InstanceItems::idARKIVETaxonomy,
    "BioLib.cz" to null,
    "BirdLife International" to InstanceItems::idBirdLifeTaxonomy,
    "Database of Vascular Plants of Canada (VASCAN)" to InstanceItems::idVASCANTaxonomy,
    "Encyclopedia of Life" to InstanceItems::idEOLTaxonomy,
    "EUNIS" to InstanceItems::idEUNISTaxonomy,
    "FishBase" to InstanceItems::idFISHBaseTaxonomy,
    "GBIF Backbone Taxonomy" to InstanceItems::idGBIFTaxonomy,
    "GRIN Taxonomy for Plants" to InstanceItems::idGRINTaxonomy,
    "iNaturalist" to InstanceItems::idINaturalistTaxonomy,
    "Index Fungorum" to InstanceItems::idIndexFungorumTaxonomy,
    "ITIS" to InstanceItems::idITISTaxonomy,
    "IUCN Red List of Threatened Species" to InstanceItems::idIUCNTaxonomy,
    "NCBI" to InstanceItems::idNCBITaxonomy,
    "Phasmida Species File" to InstanceItems::idPhasmidaTaxonomy,
    "The eBird/Clements Checklist of Birds of the World" to InstanceItems::idEBirdTaxonomy,
    "The Interim Register of Marine and Nonmarine Genera" to InstanceItems::idIRMNGTaxonomy,
    "The International Plant Names Index" to InstanceItems::idIPNITaxonomy,
    "The Mammal Species of The World" to InstanceItems::idMSWTaxonomy,
    "Tropicos - Missouri Botanical Garden" to InstanceItems::idTropicosTaxonomy,
    "uBio NameBank" to InstanceItems::idUBIOTaxonomy,
    "USDA NRCS PLANTS Database" to null,
    "World Register of Marine Species" to InstanceItems::idWORMSTaxonomy,
    "ZooBank" to InstanceItems::idZoobankTaxonomy
)

/**
 * A wikidata publishable for a taxon
 *
 * @param parentTaxon the parent taxon ID if applicable
 * @param taxonName Name of the taxon, used to find if it already exists
 * @param taxonRank Rank of the taxon used to find if it already exists
 */
data class WDTaxon(
    override var label: String,
    val parentTaxon: ItemIdValue?,
    val taxonName: String?,
    val taxonRank: RemoteItem
) : Publishable() {
    override var type: KProperty1<InstanceItems, ItemIdValue> = InstanceItems::taxon

    override fun dataStatements(): List<ReferencedStatement> =
        listOfNotNull(
            parentTaxon?.let { ReferencedValueStatement(InstanceItems::parentTaxon, it) },
            taxonName?.let { ReferencedValueStatement(InstanceItems::taxonName, it) },
            ReferencedRemoteItemStatement(InstanceItems::taxonRank, taxonRank)
        )

    override fun tryToFind(wdFinder: WDFinder, instanceItems: InstanceItems): WDTaxon {
        // In the case of the test instance, we do not have the ability to do SPARQL queries
        val resolvedTaxonRank = wdFinder.sparql.resolve(taxonRank).id
        val query =
            """
            PREFIX wd: <${InstanceItems::wdURI.get(instanceItems)}>
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            SELECT DISTINCT ?id {
              ?id wdt:${wdFinder.sparql.resolve(InstanceItems::instanceOf).id} wd:${
                wdFinder.sparql.resolve(InstanceItems::taxon).id
            };
                  wdt:${wdFinder.sparql.resolve(InstanceItems::taxonRank).id} wd:$resolvedTaxonRank;
                  wdt:${wdFinder.sparql.resolve(InstanceItems::taxonName).id} ${Rdf.literalOf(label).queryString}.
            }
            """.trimIndent()

        val results = wdFinder.sparql.query(query) { result ->
            result.map { bindingSet ->
                bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
            }
        }

        if (results.isNotEmpty()) {
            this.publishedAs(
                ItemIdValueImpl.fromId(
                    results.first(),
                    InstanceItems::wdURI.get(instanceItems)
                ) as ItemIdValue
            )
        }

        return this
    }

    /**
     * Add a property to give the ID of that taxon in a specific database
     */
    fun addTaxoDB(key: String, value: String) {
        taxDBToProperty[key]?.let { prop ->
            when (prop) {
                InstanceItems::idWORMSTaxonomy -> this.addProperty(prop, value.split(":").last())
                else -> this.addProperty(prop, value)
            }
        }
    }
}

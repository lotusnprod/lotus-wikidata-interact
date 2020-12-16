package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import kotlin.reflect.KProperty1


val taxDBToProperty: Map<String, KProperty1<InstanceItems, PropertyIdValue>?> = mapOf(
    "AmphibiaWeb" to InstanceItems::amphibiaTaxonomy,
    "ARKive" to InstanceItems::ARKIVETaxonomy,
    "BioLib.cz" to null,
    "BirdLife International" to InstanceItems::birdLifeTaxonomy,
    "Database of Vascular Plants of Canada (VASCAN)" to InstanceItems::VASCANTaxonomy,
    "Encyclopedia of Life" to InstanceItems::EOLTaxonomy,
    "EUNIS" to InstanceItems::EUNISTaxonomy,
    "FishBase" to InstanceItems::FISHBaseTaxonomy,
    "GBIF Backbone Taxonomy" to InstanceItems::GBIFTaxonomy,
    "GRIN Taxonomy for Plants" to InstanceItems::GRINTaxonomy,
    "iNaturalist" to InstanceItems::iNaturalistTaxonomy,
    "Index Fungorum" to InstanceItems::IndexFungorumTaxonomy,
    "ITIS" to InstanceItems::ITISTaxonomy,
    "IUCN Red List of Threatened Species" to InstanceItems::IUCNTaxonomy,
    "NCBI" to InstanceItems::NCBITaxonomy,
    "Phasmida Species File" to InstanceItems::phasmidaTaxonomy,
    "The eBird/Clements Checklist of Birds of the World" to InstanceItems::eBirdTaxonomy,
    "The Interim Register of Marine and Nonmarine Genera" to InstanceItems::IRMNGTaxonomy,
    "The International Plant Names Index" to InstanceItems::IPNITaxonomy,
    "The Mammal Species of The World" to InstanceItems::MSWTaxonomy,
    "Tropicos - Missouri Botanical Garden" to InstanceItems::TropicosTaxonomy,
    "uBio NameBank" to InstanceItems::uBIOTaxonomy,
    "USDA NRCS PLANTS Database" to null,
    "World Register of Marine Species" to InstanceItems::WORMSTaxonomy,
    "ZooBank" to InstanceItems::ZoobankTaxonomy
)


data class WDTaxon(
    override var name: String,
    val parentTaxon: ItemIdValue?,
    val taxonName: String?,
    val taxonRank: RemoteItem
) : Publishable() {
    override var type: KProperty1<InstanceItems, ItemIdValue> = InstanceItems::taxon

    override fun dataStatements(): List<ReferenceableStatement> =
        listOfNotNull(
            parentTaxon?.let { ReferencableValueStatement(InstanceItems::parentTaxon, it) },
            taxonName?.let { ReferencableValueStatement(InstanceItems::taxonName, it) },
            ReferenceableRemoteItemStatement(InstanceItems::taxonRank, taxonRank)
        )

    override fun tryToFind(wdFinder: WDFinder, instanceItems: InstanceItems): WDTaxon {
        // In the case of the test instance, we do not have the ability to do SPARQL queries

        val query = """
            PREFIX wd: <${InstanceItems::wdURI.get(instanceItems)}>
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            SELECT DISTINCT ?id {
              ?id wdt:${wdFinder.sparql.resolve(InstanceItems::instanceOf).id} wd:${
            wdFinder.sparql.resolve(
                InstanceItems::taxon
            ).id
        };
                  wdt:${wdFinder.sparql.resolve(InstanceItems::taxonRank).id} wd:${wdFinder.sparql.resolve(taxonRank).id};
                  wdt:${wdFinder.sparql.resolve(InstanceItems::taxonName).id} ${Rdf.literalOf(name).queryString}.
            }
            """.trimIndent()

        val results = wdFinder.sparql.query(query) { result ->
            result.map { bindingSet ->
                bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
            }
        }

        if (results.isNotEmpty()) {
            this.published(
                ItemIdValueImpl.fromId(
                    results.first(),
                    InstanceItems::wdURI.get(instanceItems)
                ) as ItemIdValue
            )
        }

        return this
    }

    fun addTaxoDB(key: String, value: String) {
        taxDBToProperty[key]?.let { prop ->
            when (prop) {
                InstanceItems::WORMSTaxonomy -> this.addProperty(prop, value.split(":").last())
                else -> this.addProperty(prop, value)
            }
        }
    }
}
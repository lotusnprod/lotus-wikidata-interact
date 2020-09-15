package net.nprod.onpdb.wdimport.wd.models

import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.sparql.ISparql
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl


val taxDBToProperty = mapOf<String, RemoteProperty?>(
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
    override var type = InstanceItems::taxon
    private val logger: Logger = LogManager.getLogger(this::class.qualifiedName)

    override fun dataStatements() =
        listOfNotNull(
            parentTaxon?.let { ReferenceableValueStatement(InstanceItems::parentTaxon, it) },
            taxonName?.let { ReferenceableValueStatement(InstanceItems::taxonName, it) },
            ReferenceableRemoteItemStatement(InstanceItems::taxonRank, taxonRank)
        )
    // TODO: Grin https://npgsweb.ars-grin.gov/gringlobal/taxonomydetail.aspx?id=12676

    override fun tryToFind(iSparql: ISparql, instanceItems: InstanceItems): WDTaxon {
        // In the case of the test instance, we do not have the ability to do SPARQL queries

        val query = """
            PREFIX wd: <${InstanceItems::wdURI.get(instanceItems)}>
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            SELECT DISTINCT ?id {
              ?id wdt:${iSparql.resolve(InstanceItems::instanceOf).id} wd:${iSparql.resolve(InstanceItems::taxon).id};
                  wdt:${iSparql.resolve(InstanceItems::taxonRank).id} wd:${iSparql.resolve(taxonRank).id};
                  wdt:${iSparql.resolve(InstanceItems::taxonName).id} ${Rdf.literalOf(name).queryString}.
            }
            """.trimIndent()
        println(query)
        val results = iSparql.query(query) { result ->
            result.map { bindingSet ->
                bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
            }
        }
        if((instanceItems.sparqlEndpoint == null) && results.isNotEmpty()) {
            logger.info("We found $results on the official instance but we can't use them here as we don't have sparql Enabled")
        }

        if (results.isNotEmpty()) {
            this.published(ItemIdValueImpl.fromId(results.first(), InstanceItems::wdURI.get(instanceItems)) as ItemIdValue)
        } else {
            logger.info("This is a new taxon!")
        }

        return this
    }

    fun addTaxoDB(key: String, value: String) {
        taxDBToProperty[key]?.let { this.addProperty(it, value) }
    }
}
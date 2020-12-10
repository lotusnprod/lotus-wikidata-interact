package net.nprod.lotus.wdimport.wd

import org.eclipse.rdf4j.sparqlbuilder.core.Prefix
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

interface InstanceItems {
    val siteIri: String
    val sitePageIri: String

    // Basics

    val statedIn: PropertyIdValue
    val instanceOf: PropertyIdValue

    // Compound

    val inChIKey: PropertyIdValue
    val inChI: PropertyIdValue
    val isomericSMILES: PropertyIdValue
    val canonicalSMILES: PropertyIdValue
    val pcId: PropertyIdValue
    val chemicalFormula: PropertyIdValue

    // Taxon
    val taxonName: PropertyIdValue
    val taxonRank: PropertyIdValue
    val parentTaxon: PropertyIdValue
    val naturalProductOfTaxon: PropertyIdValue
    val foundInTaxon: PropertyIdValue

    // Article
    val doi: PropertyIdValue
    val title: PropertyIdValue

    // Things

    val chemicalCompound: ItemIdValue
    val groupOfStereoIsomers: ItemIdValue
    val taxon: ItemIdValue
    val species: ItemIdValue
    val genus: ItemIdValue
    val family: ItemIdValue
    val scholarlyArticle: ItemIdValue

    // Sparql
    val sparqlEndpoint: String?
    val wdtURI: String
    val wdURI: String
    val pURI: String

    val wdt: Prefix
    val wd: Prefix
    val p: Prefix

    fun wdproperty(id: String): PropertyIdValue = Datamodel.makePropertyIdValue(id, wdURI)
    fun wdtproperty(id: String): PropertyIdValue = Datamodel.makePropertyIdValue(id, wdtURI)
    fun property(id: String): PropertyIdValue = Datamodel.makePropertyIdValue(id, pURI)
    fun item(id: String): ItemIdValue = Datamodel.makeItemIdValue(id, siteIri)

    val NCBITaxonomy: PropertyIdValue
    val IRMNGTaxonomy: PropertyIdValue
    val GBIFTaxonomy: PropertyIdValue
    val ITISTaxonomy: PropertyIdValue
    val IPNITaxonomy: PropertyIdValue
    val uBIOTaxonomy: PropertyIdValue
    val GRINTaxonomy: PropertyIdValue
    val EOLTaxonomy: PropertyIdValue
    val TropicosTaxonomy: PropertyIdValue
    val iNaturalistTaxonomy: PropertyIdValue
    val VASCANTaxonomy: PropertyIdValue
    val EUNISTaxonomy: PropertyIdValue
    val IndexFungorumTaxonomy: PropertyIdValue
    val IUCNTaxonomy: PropertyIdValue
    val WORMSTaxonomy: PropertyIdValue
    val FISHBaseTaxonomy: PropertyIdValue
    val ARKIVETaxonomy: PropertyIdValue
    val MSWTaxonomy: PropertyIdValue
    val ZoobankTaxonomy: PropertyIdValue
    val eBirdTaxonomy: PropertyIdValue
    val birdLifeTaxonomy: PropertyIdValue
    val amphibiaTaxonomy: PropertyIdValue
    val phasmidaTaxonomy: PropertyIdValue
}


object TestInstanceItems : InstanceItems {
    override val siteIri: String = "http://test.wikidata.org/entity/"
    override val sitePageIri: String = "https://test.wikidata.org/w/index.php?title="
    override val pURI: String = "http://test.wikidata.org/prop/"
    override val wdtURI: String = "http://test.wikidata.org/prop/direct/"
    override val wdURI: String = "http://test.wikidata.org/entity/"

    //override val ENDPOINT = "https://query.wikidata.org/bigdata/namespace/wdq/sparql"
    override val sparqlEndpoint: String? = null
    override val wdt: Prefix = SparqlBuilder.prefix("wdt", Rdf.iri(wdtURI))
    override val wd: Prefix = SparqlBuilder.prefix("wd", Rdf.iri(wdURI))
    override val p: Prefix = SparqlBuilder.prefix("p", Rdf.iri(pURI))

    // Properties
    override val inChIKey: PropertyIdValue = property("P95461")
    override val inChI: PropertyIdValue = property("P95462")
    override val isomericSMILES: PropertyIdValue = property("P95463")
    override val canonicalSMILES: PropertyIdValue = property("P95466")
    override val pcId: PropertyIdValue = property("P95464")
    override val chemicalFormula: PropertyIdValue = property("P95465")
    override val instanceOf: PropertyIdValue = wdproperty("P82")

    override val taxonName: PropertyIdValue = wdtproperty("P95494") // This is a random one
    override val taxonRank: PropertyIdValue = property("P522")
    override val parentTaxon: PropertyIdValue = property("P2105")

    override val NCBITaxonomy: PropertyIdValue = property("P95472")
    override val IRMNGTaxonomy: PropertyIdValue = property("P95477")
    override val GBIFTaxonomy: PropertyIdValue = property("P95480")
    override val ITISTaxonomy: PropertyIdValue = property("P95484")
    override val IPNITaxonomy: PropertyIdValue = property("P95489")
    override val uBIOTaxonomy: PropertyIdValue = property("P95492")
    override val GRINTaxonomy: PropertyIdValue = property("P95494")
    override val EOLTaxonomy: PropertyIdValue = property("P95497")
    override val TropicosTaxonomy: PropertyIdValue = property("P95500")
    override val iNaturalistTaxonomy: PropertyIdValue = property("P95503")
    override val VASCANTaxonomy: PropertyIdValue = property("P95505")
    override val EUNISTaxonomy: PropertyIdValue = property("P95508")
    override val IndexFungorumTaxonomy: PropertyIdValue = property("P95511")
    override val IUCNTaxonomy: PropertyIdValue = property("P95513")
    override val WORMSTaxonomy: PropertyIdValue = property("P95517")
    override val FISHBaseTaxonomy: PropertyIdValue = property("P95519")
    override val ARKIVETaxonomy: PropertyIdValue = property("P95522")
    override val MSWTaxonomy: PropertyIdValue = property("P95525")
    override val ZoobankTaxonomy: PropertyIdValue = property("P95527")
    override val eBirdTaxonomy: PropertyIdValue = property("P95532")
    override val birdLifeTaxonomy: PropertyIdValue = property("P95534")
    override val amphibiaTaxonomy: PropertyIdValue = property("P95537")
    override val phasmidaTaxonomy: PropertyIdValue = property("P95540")

    override val naturalProductOfTaxon: PropertyIdValue = property("P95470")
    override val foundInTaxon: PropertyIdValue = property("P95646")
    override val statedIn: PropertyIdValue = property("P149")

    // Article
    override val doi: PropertyIdValue = property("P168")
    override val title: PropertyIdValue = property("P95645")

    // Things
    override val chemicalCompound: ItemIdValue = item("Q212525")
    override val groupOfStereoIsomers: ItemIdValue = item("Q59199015")
    override val taxon: ItemIdValue = item("Q212541")
    override val species: ItemIdValue = item("Q212542")
    override val genus: ItemIdValue = item("Q212543")
    override val family: ItemIdValue = item("Q212544")
    override val scholarlyArticle: ItemIdValue = item("Q212556")
}

object MainInstanceItems : InstanceItems {
    override val siteIri: String = "http://www.wikidata.org/entity/"
    override val sitePageIri: String = "https://www.wikidata.org/w/index.php?title="

    override val pURI: String = "http://www.wikidata.org/prop/direct/"
    override val wdtURI: String = "http://www.wikidata.org/prop/direct/"
    override val wdURI: String = "http://www.wikidata.org/entity/"
    override val sparqlEndpoint: String = "https://query.wikidata.org/bigdata/namespace/wdq/sparql"

    override val wdt: Prefix = SparqlBuilder.prefix("wdt", Rdf.iri(wdtURI))
    override val wd: Prefix = SparqlBuilder.prefix("wd", Rdf.iri(wdURI))
    override val p: Prefix = SparqlBuilder.prefix("p", Rdf.iri(pURI))

    // Properties
    override val inChIKey: PropertyIdValue = property("P235")
    override val inChI: PropertyIdValue = property("P234")
    override val isomericSMILES: PropertyIdValue = property("P2017")
    override val canonicalSMILES: PropertyIdValue = property("P233")
    override val pcId: PropertyIdValue = property("P662")
    override val chemicalFormula: PropertyIdValue = property("P274")
    override val instanceOf: PropertyIdValue = property("P31")

    // Properties of taxa
    override val taxonName: PropertyIdValue = property("P225")
    override val taxonRank: PropertyIdValue = property("P105")
    override val parentTaxon: PropertyIdValue = property("P171")

    override val NCBITaxonomy: PropertyIdValue = property("P685")
    override val IRMNGTaxonomy: PropertyIdValue = property("P5055")
    override val GBIFTaxonomy: PropertyIdValue = property("P846")
    override val ITISTaxonomy: PropertyIdValue = property("P815")
    override val IPNITaxonomy: PropertyIdValue = property("P961")
    override val uBIOTaxonomy: PropertyIdValue = property("P4728")
    override val GRINTaxonomy: PropertyIdValue = property("P1421")
    override val EOLTaxonomy: PropertyIdValue = property("P830")
    override val TropicosTaxonomy: PropertyIdValue = property("P960")
    override val iNaturalistTaxonomy: PropertyIdValue = property("P3151")
    override val VASCANTaxonomy: PropertyIdValue = property("P1745")
    override val EUNISTaxonomy: PropertyIdValue = property("P6177")
    override val IndexFungorumTaxonomy: PropertyIdValue = property("P1391")
    override val IUCNTaxonomy: PropertyIdValue = property("P627")
    override val WORMSTaxonomy: PropertyIdValue = property("P850")
    override val FISHBaseTaxonomy: PropertyIdValue = property("P938")
    override val ARKIVETaxonomy: PropertyIdValue = property("P2833")
    override val MSWTaxonomy: PropertyIdValue = property("P959")
    override val ZoobankTaxonomy: PropertyIdValue = property("P1746")
    override val eBirdTaxonomy: PropertyIdValue = property("P3444")
    override val birdLifeTaxonomy: PropertyIdValue = property("P5257")
    override val amphibiaTaxonomy: PropertyIdValue = property("P5036")
    override val phasmidaTaxonomy: PropertyIdValue = property("P4855")

    override val naturalProductOfTaxon: PropertyIdValue = property("P1582")
    override val foundInTaxon: PropertyIdValue = property("P703")
    override val statedIn: PropertyIdValue = property("P248")

    // Article
    override val doi: PropertyIdValue = property("P356")
    override val title: PropertyIdValue = property("P1476")


    // Things
    override val chemicalCompound: ItemIdValue = item("Q11173")
    override val groupOfStereoIsomers: ItemIdValue = item("Q59199015")
    override val taxon: ItemIdValue = item("Q16521")
    override val species: ItemIdValue = item("Q7432")
    override val genus: ItemIdValue = item("Q34740")
    override val family: ItemIdValue = item("Q35409")
    override val scholarlyArticle: ItemIdValue = item("Q13442814")
}
package net.nprod.onpdb.wdimport.wd

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

    // Article
    val doi: PropertyIdValue
    val title: PropertyIdValue

    // Things

    val chemicalCompound: ItemIdValue
    val taxon: ItemIdValue
    val species: ItemIdValue
    val genus: ItemIdValue
    val scholarlyArticle: ItemIdValue

    // Sparql
    val sparqlEndpoint: String?
    val wdtURI: String
    val wdURI: String
    val pURI: String

    val wdt: Prefix
    val wd: Prefix
    val p: Prefix

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
    override val siteIri = "http://www.test.wikidata.org/entity/"
    override val sitePageIri = "https://test.wikidata.org/w/index.php?title="
    override val pURI = "http://test.wikidata.org/prop/direct/"
    override val wdtURI = "http://test.wikidata.org/prop/direct/"
    override val wdURI = "http://test.wikidata.org/entity/"

    //override val ENDPOINT = "https://query.wikidata.org/bigdata/namespace/wdq/sparql"
    override val sparqlEndpoint: String? = null
    override val wdt: Prefix = SparqlBuilder.prefix("wdt", Rdf.iri(wdtURI))
    override val wd: Prefix = SparqlBuilder.prefix("wd", Rdf.iri(wdURI))
    override val p: Prefix = SparqlBuilder.prefix("p", Rdf.iri(pURI))

    // Properties
    override val inChIKey = property("P95461")
    override val inChI = property("P95462")
    override val isomericSMILES = property("P95463")
    override val canonicalSMILES = property("P95466")
    override val pcId = property("P95464")
    override val chemicalFormula = property("P95465")
    override val instanceOf = property("P82")

    override val taxonName = property("P49")
    override val taxonRank = property("P522")
    override val parentTaxon = property("P2105")

    override val NCBITaxonomy = property("P95472")
    override val IRMNGTaxonomy = property("P95477")
    override val GBIFTaxonomy = property("P95480")
    override val ITISTaxonomy = property("P95484")
    override val IPNITaxonomy = property("P95489")
    override val uBIOTaxonomy = property("P95492")
    override val GRINTaxonomy = property("P95494")
    override val EOLTaxonomy = property("P95497")
    override val TropicosTaxonomy = property("P95500")
    override val iNaturalistTaxonomy = property("P95503")
    override val VASCANTaxonomy = property("P95505")
    override val EUNISTaxonomy = property("P95508")
    override val IndexFungorumTaxonomy = property("P95511")
    override val IUCNTaxonomy = property("P95513")
    override val WORMSTaxonomy = property("P95517")
    override val FISHBaseTaxonomy = property("P95519")
    override val ARKIVETaxonomy = property("P95522")
    override val MSWTaxonomy = property("P95525")
    override val ZoobankTaxonomy = property("P95527")
    override val eBirdTaxonomy = property("P95532")
    override val birdLifeTaxonomy = property("P95534")
    override val amphibiaTaxonomy = property("P95537")
    override val phasmidaTaxonomy = property("P95540")

    override val naturalProductOfTaxon = property("P95470")
    override val statedIn = property("P149")

    // Article
    override val doi = property("P168")
    override val title = property("P95")

    // Things
    override val chemicalCompound = item("Q212525")
    override val taxon = item("Q212541")
    override val species = item("Q212542")
    override val genus = item("Q212543")
    override val scholarlyArticle = item("Q212556")
}

object MainInstanceItems : InstanceItems {
    override val siteIri = "http://www.wikidata.org/entity/"
    override val sitePageIri = "https://www.wikidata.org/w/index.php?title="

    override val pURI = "http://www.wikidata.org/prop/direct/"
    override val wdtURI = "http://www.wikidata.org/prop/direct/"
    override val wdURI = "http://www.wikidata.org/entity/"
    override val sparqlEndpoint = "https://query.wikidata.org/bigdata/namespace/wdq/sparql"

    override val wdt: Prefix = SparqlBuilder.prefix("wdt", Rdf.iri(wdtURI))
    override val wd: Prefix = SparqlBuilder.prefix("wd", Rdf.iri(wdURI))
    override val p: Prefix = SparqlBuilder.prefix("p", Rdf.iri(pURI))

    // Properties
    override val inChIKey = property("P235")
    override val inChI = property("P234")
    override val isomericSMILES = property("P2017")
    override val canonicalSMILES = property("P233")
    override val pcId = property("P662")
    override val chemicalFormula = property("P274")
    override val instanceOf = property("P31")

    // Properties of taxons
    override val taxonName = property("P225")
    override val taxonRank = property("P105")
    override val parentTaxon = property("P171")

    override val NCBITaxonomy = property("P685")
    override val IRMNGTaxonomy = property("P5055")
    override val GBIFTaxonomy = property("P846")
    override val ITISTaxonomy = property("P815")
    override val IPNITaxonomy = property("P961")
    override val uBIOTaxonomy = property("P4728")
    override val GRINTaxonomy = property("P1421")
    override val EOLTaxonomy = property("P830")
    override val TropicosTaxonomy = property("P960")
    override val iNaturalistTaxonomy = property("P3151")
    override val VASCANTaxonomy = property("P1745")
    override val EUNISTaxonomy = property("P6177")
    override val IndexFungorumTaxonomy = property("P1391")
    override val IUCNTaxonomy = property("P627")
    override val WORMSTaxonomy = property("P850")
    override val FISHBaseTaxonomy = property("P938")
    override val ARKIVETaxonomy = property("P2833")
    override val MSWTaxonomy = property("P959")
    override val ZoobankTaxonomy = property("P1746")
    override val eBirdTaxonomy = property("P3444")
    override val birdLifeTaxonomy = property("P5257")
    override val amphibiaTaxonomy = property("P5036")
    override val phasmidaTaxonomy = property("P4855")

    override val naturalProductOfTaxon = property("P1582")
    override val statedIn = property("P248")

    // Article
    override val doi = property("P356")
    override val title = property("P1476")


    // Things
    override val chemicalCompound = item("Q11173")
    override val taxon = item("Q16521")
    override val species = item("Q7432")
    override val genus = item("Q34740")
    override val scholarlyArticle = item("Q13442814")
}
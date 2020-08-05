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

    val wdt: Prefix
    val wd: Prefix

    fun property(id: String): PropertyIdValue = Datamodel.makePropertyIdValue(id, siteIri)
    fun item(id: String): ItemIdValue = Datamodel.makeItemIdValue(id, siteIri)
}


object TestInstanceItems : InstanceItems {
    override val siteIri = "http://www.test.wikidata.org/entity/"
    override val sitePageIri = "https://test.wikidata.org/w/index.php?title="

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

    // Sparql

    override val wdtURI = "http://test.wikidata.org/prop/direct/"
    override val wdURI = "http://test.wikidata.org/entity/"
    //override val ENDPOINT = "https://query.wikidata.org/bigdata/namespace/wdq/sparql"
    override val sparqlEndpoint: String? = null
    override val wdt: Prefix = SparqlBuilder.prefix("wdt", Rdf.iri(wdtURI))
    override val wd: Prefix = SparqlBuilder.prefix("net/nprod/onpdb/wdimport/wd", Rdf.iri(wdURI))
}

object MainInstanceItems : InstanceItems {
    override val siteIri = "http://www.wikidata.org/entity/"
    override val sitePageIri = "https://www.wikidata.org/w/index.php?title="

    // Properties
    override val inChIKey = property("P235")
    override val inChI = property("P234")
    override val isomericSMILES = property("P2017")
    override val canonicalSMILES = property("P233")
    override val pcId = property("P662")
    override val chemicalFormula = property("P274")
    override val instanceOf = property("P31")

    override val taxonName = property("P225")
    override val taxonRank = property("P105")
    override val parentTaxon = property("P171")
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

    // Sparql

    override val wdtURI = "http://www.wikidata.org/prop/direct/"
    override val wdURI = "http://www.wikidata.org/entity/"
    override val sparqlEndpoint = "https://query.wikidata.org/bigdata/namespace/wdq/sparql"
    override val wdt: Prefix = SparqlBuilder.prefix("wdt", Rdf.iri(wdtURI))
    override val wd: Prefix = SparqlBuilder.prefix("net/nprod/onpdb/wdimport/wd", Rdf.iri(wdURI))
}
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

@file:Suppress("KDocMissingDocumentation")

package net.nprod.lotus.wdimport.wd

import org.eclipse.rdf4j.sparqlbuilder.core.Prefix
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

/**
 * Instance items are the collection of IDs for important subjects and properties from WikiData
 *
 * As the test instance does not have the same IDs as the official instance, we needed something like that
 */
interface InstanceItems {
    /**
     * The wikidata site IRI
     */
    val siteIri: String

    /**
     * The wikidata site page IRI
     */
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
    val mass: PropertyIdValue

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
    val variety: ItemIdValue
    val species: ItemIdValue
    val genus: ItemIdValue
    val subtribe: ItemIdValue
    val tribe: ItemIdValue
    val subfamily: ItemIdValue
    val family: ItemIdValue
    val order: ItemIdValue
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

    val idNCBITaxonomy: PropertyIdValue
    val idIRMNGTaxonomy: PropertyIdValue
    val idGBIFTaxonomy: PropertyIdValue
    val idITISTaxonomy: PropertyIdValue
    val idIPNITaxonomy: PropertyIdValue
    val idUBIOTaxonomy: PropertyIdValue
    val idGRINTaxonomy: PropertyIdValue
    val idEOLTaxonomy: PropertyIdValue
    val idTropicosTaxonomy: PropertyIdValue
    val idINaturalistTaxonomy: PropertyIdValue
    val idVASCANTaxonomy: PropertyIdValue
    val idEUNISTaxonomy: PropertyIdValue
    val idIndexFungorumTaxonomy: PropertyIdValue
    val idIUCNTaxonomy: PropertyIdValue
    val idWORMSTaxonomy: PropertyIdValue
    val idFISHBaseTaxonomy: PropertyIdValue
    val idARKIVETaxonomy: PropertyIdValue
    val idMSWTaxonomy: PropertyIdValue
    val idZoobankTaxonomy: PropertyIdValue
    val idEBirdTaxonomy: PropertyIdValue
    val idBirdLifeTaxonomy: PropertyIdValue
    val idAmphibiaTaxonomy: PropertyIdValue
    val idPhasmidaTaxonomy: PropertyIdValue
    val idOTLTaxonomy: PropertyIdValue
    val idAlgaeBaseTaxonomy: PropertyIdValue
    val idWFOTaxonomy: PropertyIdValue
    val idCOLTaxonomy: PropertyIdValue
    val pmid: PropertyIdValue
    val pmcid: PropertyIdValue
    val arxiv: PropertyIdValue
    val crossref: ItemIdValue
    val publication: ItemIdValue
    val publishedIn: PropertyIdValue
    val seriesOrdinal: PropertyIdValue
    val volume: PropertyIdValue
    val pages: PropertyIdValue
    val issue: PropertyIdValue
    val authorNameString: PropertyIdValue
    val author: PropertyIdValue
    val referenceURL: PropertyIdValue
    val orcid: PropertyIdValue
    val retrieved: PropertyIdValue
    val issn: PropertyIdValue
    val publicationDate: PropertyIdValue
}

object TestInstanceItems : InstanceItems {
    override val siteIri: String = "http://test.wikidata.org/entity/"
    override val sitePageIri: String = "https://test.wikidata.org/w/index.php?title="
    override val pURI: String = "http://test.wikidata.org/prop/"
    override val wdtURI: String = "http://test.wikidata.org/prop/direct/"
    override val wdURI: String = "http://test.wikidata.org/entity/"

    // override val ENDPOINT = "https://query.wikidata.org/bigdata/namespace/wdq/sparql"
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
    override val mass: PropertyIdValue = property("P95467")
    override val instanceOf: PropertyIdValue = wdproperty("P82")

    override val taxonName: PropertyIdValue = wdtproperty("P95494") // This is a random one
    override val taxonRank: PropertyIdValue = property("P522")
    override val parentTaxon: PropertyIdValue = property("P2105")

    override val idNCBITaxonomy: PropertyIdValue = property("P95472")
    override val idIRMNGTaxonomy: PropertyIdValue = property("P95477")
    override val idGBIFTaxonomy: PropertyIdValue = property("P95480")
    override val idITISTaxonomy: PropertyIdValue = property("P95484")
    override val idIPNITaxonomy: PropertyIdValue = property("P95489")
    override val idUBIOTaxonomy: PropertyIdValue = property("P95492")
    override val idGRINTaxonomy: PropertyIdValue = property("P95494")
    override val idEOLTaxonomy: PropertyIdValue = property("P95497")
    override val idTropicosTaxonomy: PropertyIdValue = property("P95500")
    override val idINaturalistTaxonomy: PropertyIdValue = property("P95503")
    override val idVASCANTaxonomy: PropertyIdValue = property("P95505")
    override val idEUNISTaxonomy: PropertyIdValue = property("P95508")
    override val idIndexFungorumTaxonomy: PropertyIdValue = property("P95511")
    override val idIUCNTaxonomy: PropertyIdValue = property("P95513")
    override val idWORMSTaxonomy: PropertyIdValue = property("P95517")
    override val idFISHBaseTaxonomy: PropertyIdValue = property("P95519")
    override val idARKIVETaxonomy: PropertyIdValue = property("P95522")
    override val idMSWTaxonomy: PropertyIdValue = property("P95525")
    override val idZoobankTaxonomy: PropertyIdValue = property("P95527")
    override val idEBirdTaxonomy: PropertyIdValue = property("P95532")
    override val idBirdLifeTaxonomy: PropertyIdValue = property("P95534")
    override val idAmphibiaTaxonomy: PropertyIdValue = property("P95537")
    override val idPhasmidaTaxonomy: PropertyIdValue = property("P95540")
    override val idOTLTaxonomy: PropertyIdValue = property("P95542")
    override val idAlgaeBaseTaxonomy: PropertyIdValue = property("P95545")
    override val idWFOTaxonomy: PropertyIdValue = property("P95549")
    override val idCOLTaxonomy: PropertyIdValue = property("P95554")

    override val naturalProductOfTaxon: PropertyIdValue = property("P95470")
    override val foundInTaxon: PropertyIdValue = property("P95646")

    // Article
    override val doi: PropertyIdValue = property("P168")
    override val pmid: PropertyIdValue = property("P698")
    override val pmcid: PropertyIdValue = property("P932")
    override val arxiv: PropertyIdValue = property("P932")
    override val title: PropertyIdValue = property("P95645")
    override val publishedIn: PropertyIdValue = property("P1433")
    override val publicationDate: PropertyIdValue = property("P577")
    override val volume: PropertyIdValue = property("P478")
    override val pages: PropertyIdValue = property("P304")
    override val issue: PropertyIdValue = property("P433")
    override val issn: PropertyIdValue = property("P236")

    override val authorNameString: PropertyIdValue = property("P2093")
    override val author: PropertyIdValue = property("P50")
    override val orcid: PropertyIdValue = property("P496")

    override val referenceURL: PropertyIdValue = property("P854")

    override val crossref: ItemIdValue = item("Q5188229")

    override val scholarlyArticle: ItemIdValue = item("Q212556")
    override val publication: ItemIdValue = item("Q732577")

    // Things
    override val chemicalCompound: ItemIdValue = item("Q212525")
    override val groupOfStereoIsomers: ItemIdValue = item("Q59199015")

    // Taxonomy
    override val taxon: ItemIdValue = item("Q212541")
    override val variety: ItemIdValue = item("Q767728")
    override val species: ItemIdValue = item("Q212542")
    override val genus: ItemIdValue = item("Q212543")
    override val subtribe: ItemIdValue = item("Q3965313")
    override val tribe: ItemIdValue = item("Q227936")
    override val subfamily: ItemIdValue = item("Q164280")
    override val family: ItemIdValue = item("Q212544")
    override val order: ItemIdValue = item("Q36602")

    override val seriesOrdinal: PropertyIdValue = property("P1545")

    // Reference
    override val statedIn: PropertyIdValue = property("P149")
    override val retrieved: PropertyIdValue = property("P813")
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
    override val mass: PropertyIdValue = property("P2067")
    override val instanceOf: PropertyIdValue = property("P31")

    // Properties of taxa
    override val taxonName: PropertyIdValue = property("P225")
    override val taxonRank: PropertyIdValue = property("P105")
    override val parentTaxon: PropertyIdValue = property("P171")

    override val idNCBITaxonomy: PropertyIdValue = property("P685")
    override val idIRMNGTaxonomy: PropertyIdValue = property("P5055")
    override val idGBIFTaxonomy: PropertyIdValue = property("P846")
    override val idITISTaxonomy: PropertyIdValue = property("P815")
    override val idIPNITaxonomy: PropertyIdValue = property("P961")
    override val idUBIOTaxonomy: PropertyIdValue = property("P4728")
    override val idGRINTaxonomy: PropertyIdValue = property("P1421")
    override val idEOLTaxonomy: PropertyIdValue = property("P830")
    override val idTropicosTaxonomy: PropertyIdValue = property("P960")
    override val idINaturalistTaxonomy: PropertyIdValue = property("P3151")
    override val idVASCANTaxonomy: PropertyIdValue = property("P1745")
    override val idEUNISTaxonomy: PropertyIdValue = property("P6177")
    override val idIndexFungorumTaxonomy: PropertyIdValue = property("P1391")
    override val idIUCNTaxonomy: PropertyIdValue = property("P627")
    override val idWORMSTaxonomy: PropertyIdValue = property("P850")
    override val idFISHBaseTaxonomy: PropertyIdValue = property("P938")
    override val idARKIVETaxonomy: PropertyIdValue = property("P2833")
    override val idMSWTaxonomy: PropertyIdValue = property("P959")
    override val idZoobankTaxonomy: PropertyIdValue = property("P1746")
    override val idEBirdTaxonomy: PropertyIdValue = property("P3444")
    override val idBirdLifeTaxonomy: PropertyIdValue = property("P5257")
    override val idAmphibiaTaxonomy: PropertyIdValue = property("P5036")
    override val idPhasmidaTaxonomy: PropertyIdValue = property("P4855")
    override val idOTLTaxonomy: PropertyIdValue = property("P9157")
    override val idAlgaeBaseTaxonomy: PropertyIdValue = property("P1348")
    override val idWFOTaxonomy: PropertyIdValue = property("P7715")
    override val idCOLTaxonomy: PropertyIdValue = property("P10585")

    override val naturalProductOfTaxon: PropertyIdValue = property("P1582")
    override val foundInTaxon: PropertyIdValue = property("P703")

    // Article
    override val doi: PropertyIdValue = property("P356")
    override val pmid: PropertyIdValue = property("P698")
    override val pmcid: PropertyIdValue = property("P932")
    override val arxiv: PropertyIdValue = property("P932")
    override val title: PropertyIdValue = property("P1476")
    override val publishedIn: PropertyIdValue = property("P1433")
    override val publicationDate: PropertyIdValue = property("P577")
    override val volume: PropertyIdValue = property("P478")
    override val pages: PropertyIdValue = property("P304")
    override val issue: PropertyIdValue = property("P433")
    override val issn: PropertyIdValue = property("P236")

    override val authorNameString: PropertyIdValue = property("P2093")
    override val author: PropertyIdValue = property("P50")
    override val orcid: PropertyIdValue = property("P496")

    override val referenceURL: PropertyIdValue = property("P854")

    override val crossref: ItemIdValue = item("Q5188229")

    override val scholarlyArticle: ItemIdValue = item("Q13442814")
    override val publication: ItemIdValue = item("Q732577")

    // Things
    override val chemicalCompound: ItemIdValue = item("Q11173")
    override val groupOfStereoIsomers: ItemIdValue = item("Q59199015")

    // Taxonomy
    override val taxon: ItemIdValue = item("Q16521")
    override val variety: ItemIdValue = item("Q767728")
    override val species: ItemIdValue = item("Q7432")
    override val genus: ItemIdValue = item("Q34740")
    override val subtribe: ItemIdValue = item("Q3965313")
    override val tribe: ItemIdValue = item("Q227936")
    override val subfamily: ItemIdValue = item("Q164280")
    override val family: ItemIdValue = item("Q35409")
    override val order: ItemIdValue = item("Q36602")

    override val seriesOrdinal: PropertyIdValue = property("P1545")

    // Reference
    override val statedIn: PropertyIdValue = property("P248")
    override val retrieved: PropertyIdValue = property("P813")
}

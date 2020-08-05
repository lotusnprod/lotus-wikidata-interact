package wd

import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

interface InstanceItems {
    val siteIri: String
    val inChIKey: PropertyIdValue
    val inChI: PropertyIdValue
    val isomericSMILES: PropertyIdValue
    val canonicalSMILES: PropertyIdValue
    val pcId: PropertyIdValue
    val chemicalFormula: PropertyIdValue
    val instanceOf: PropertyIdValue
    val taxonName: PropertyIdValue
    val taxonRank: PropertyIdValue
    val parentTaxon: PropertyIdValue
    val naturalProductOfTaxon: PropertyIdValue

    val chemicalCompound: ItemIdValue
    val taxon: ItemIdValue
    val species: ItemIdValue
    val genus: ItemIdValue

    fun property(id: String): PropertyIdValue = Datamodel.makePropertyIdValue(id, siteIri)
    fun item(id: String): ItemIdValue = Datamodel.makeItemIdValue(id, siteIri)
}


object TestInstanceItems : InstanceItems {
    override val siteIri = "http://www.test.wikidata.org/entity/"

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

    // Things
    override val chemicalCompound = item("Q212525")
    override val taxon = item("Q212541")
    override val species = item("Q212542")
    override val genus = item("Q212543")
}
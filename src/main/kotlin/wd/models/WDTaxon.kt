package wd.models

import org.wikidata.wdtk.datamodel.interfaces.*
import wd.InstanceItems
import wd.newDocument
import wd.statement

// TODO: Identifiers

data class WDTaxon(
    val name: String,
    val parentTaxon: ItemIdValue?,
    val taxonName: String,
    val taxonRank: ItemIdValue
): Publishable() {
    override fun document(instanceItems: InstanceItems): ItemDocument {
        require(!this.published) { "Cannot request the document of an already published item."}
        return newDocument(name) {
            statement(instanceItems.instanceOf, instanceItems.taxon)
            parentTaxon?.let { statement(instanceItems.parentTaxon, parentTaxon) }
            statement(instanceItems.taxonName, taxonName)
            statement(instanceItems.taxonRank, taxonRank)
        }
    }
}
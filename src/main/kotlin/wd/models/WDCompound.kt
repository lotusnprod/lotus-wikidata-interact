package wd.models

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import wd.InstanceItems
import wd.newDocument
import wd.statement
import kotlin.reflect.KProperty1

data class WDCompound(
    val name: String,
    val inChIKey: String,
    val inChI: String,
    val isomericSMILES: String,
    val pcId: String?,
    val chemicalFormula: String
) : Publishable() {
    /**
     * pre-statements are added and will be resolved once the document is requested.
     */
    private val preStatements: MutableList<Pair<KProperty1<InstanceItems, PropertyIdValue>, ItemIdValue>> =
        mutableListOf()

    override fun document(instanceItems: InstanceItems): ItemDocument {
        require(!this.published) { "Cannot request the document of an already published item."}
        return newDocument(name) {
            statement(instanceItems.instanceOf, instanceItems.chemicalCompound)
            statement(instanceItems.inChIKey, inChIKey)
            statement(instanceItems.inChI, inChI)
            statement(instanceItems.isomericSMILES, isomericSMILES)
            statement(instanceItems.chemicalFormula, chemicalFormula)

            // We construct the statements according to this instanceItems value
            preStatements.forEach {
                statement(it.first.get(instanceItems), it.second)
            }
            preStatements.clear()
            pcId?.let { statement(instanceItems.pcId, it) }
        }
    }

    fun addNaturalProductOfTaxon(wdTaxon: WDTaxon) {
        require(wdTaxon.published) { "Can only add an already published taxon." }
        preStatements.add(Pair(InstanceItems::naturalProductOfTaxon, wdTaxon.id))
    }
}
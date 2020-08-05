package wd.models

import org.wikidata.wdtk.datamodel.interfaces.*
import wd.InstanceItems
import wd.newDocument
import wd.statement


data class WDOrganism(
    val name: String,
    val inChIKey: String,
    val inChI: String,
    val isomericSMILES: String,
    val pcId: String?,
    val chemicalFormula: String
) {
    fun document(instanceItems: InstanceItems): ItemDocument {
        return newDocument(name) {
            statement(instanceItems.instanceOf, instanceItems.chemicalCompound)
            statement(instanceItems.inChIKey, inChIKey)
            statement(instanceItems.inChI, inChI)
            statement(instanceItems.isomericSMILES, isomericSMILES)
            statement(instanceItems.chemicalFormula, chemicalFormula)
            pcId?.let { statement(instanceItems.pcId, it) }
        }
    }
}
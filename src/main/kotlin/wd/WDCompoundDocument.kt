package wd

import org.wikidata.wdtk.datamodel.interfaces.*


data class WDCompound(
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
package wd

import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.newDocument
import net.nprod.onpdb.wdimport.wd.statement
import org.wikidata.wdtk.datamodel.interfaces.*


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
            statement(null, instanceItems.instanceOf, instanceItems.chemicalCompound)
            statement(null, instanceItems.inChIKey, inChIKey)
            statement(null, instanceItems.inChI, inChI)
            statement(null, instanceItems.isomericSMILES, isomericSMILES)
            statement(null, instanceItems.chemicalFormula, chemicalFormula)
            pcId?.let { statement(null, instanceItems.pcId, it) }
        }
    }
}
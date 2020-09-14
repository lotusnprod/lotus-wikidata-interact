package net.nprod.onpdb.wdimport.wd.models

import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.sparql.ISparql
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql

data class WDCompound(
    override var name: String,
    val inChIKey: String?,
    val inChI: String?,
    val isomericSMILES: String?,
    val pcId: String?,
    val chemicalFormula: String?,
    val f: WDCompound.() -> Unit = {}
) : Publishable() {
    override var type = InstanceItems::chemicalCompound

    init {
        apply(f)
    }

    override fun dataStatements() =
        listOfNotNull(
            inChIKey?.let { ReferenceableValueStatement(InstanceItems::inChIKey, it) },
            inChI?.let { ReferenceableValueStatement(InstanceItems::inChI, it) },
            isomericSMILES?.let { ReferenceableValueStatement(InstanceItems::isomericSMILES, it) },
            chemicalFormula?.let { ReferenceableValueStatement(InstanceItems::chemicalFormula, it) },
            pcId?.let { ReferenceableValueStatement(InstanceItems::pcId, it) }
        )

    override fun tryToFind(iSparql: ISparql, instanceItems: InstanceItems): Publishable {
        TODO("Not yet implemented")
    }

    fun naturalProductOfTaxon(wdTaxon: WDTaxon, f: ReferenceableValueStatement.() -> Unit) {
        require(wdTaxon.published) { "Can only add an already published taxon." }
        val refStatement = ReferenceableValueStatement(InstanceItems::naturalProductOfTaxon, wdTaxon.id).apply(f)
        preStatements.add(refStatement)
    }
}
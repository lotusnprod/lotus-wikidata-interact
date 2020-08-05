package net.nprod.onpdb.wdimport.wd.models

import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql

// TODO: Identifiers

data class WDArticle(
    override var name: String,
    val title: String?,
    val doi: String?
): Publishable() {
    override var type = InstanceItems::scholarlyArticle

    override fun dataStatements() =
        listOfNotNull(
            title?.let { ReferenceableValueStatement(InstanceItems::title, it) },
            doi?.let { ReferenceableValueStatement(InstanceItems::doi, it) })

    override fun tryToFind(wdSparql: WDSparql, instanceItems: InstanceItems): Publishable {
        TODO("Not yet implemented")
    }

}
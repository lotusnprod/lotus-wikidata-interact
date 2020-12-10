package net.nprod.lotus.wdimport.wd

import net.nprod.lotus.wdimport.wd.models.RemoteItem
import net.nprod.lotus.wdimport.wd.models.RemoteProperty
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

/**
 * Resolve the given element according to the instanceItem of that item
 */
interface Resolver {
    val instanceItems: InstanceItems
    fun resolve(item: RemoteItem): ItemIdValue = item.get(instanceItems)
    fun resolve(item: RemoteProperty): PropertyIdValue = item.get(instanceItems)
}
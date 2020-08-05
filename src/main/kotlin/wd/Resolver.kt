package wd

import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import wd.models.RemoteItem
import wd.models.RemoteProperty

/**
 * Resolve the given element according to the instanceItem of that item
 */
interface Resolver {
    val instanceItems: InstanceItems
    fun resolve(item: RemoteItem): ItemIdValue = item.get(instanceItems)
    fun resolve(item: RemoteProperty): PropertyIdValue = item.get(instanceItems)
}
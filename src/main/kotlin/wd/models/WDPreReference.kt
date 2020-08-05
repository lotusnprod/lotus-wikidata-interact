package wd.models

import org.wikidata.wdtk.datamodel.helpers.ReferenceBuilder
import org.wikidata.wdtk.datamodel.interfaces.Reference
import org.wikidata.wdtk.datamodel.interfaces.Value
import wd.InstanceItems


data class PropertyStore(
    val property: RemoteProperty,
    val value: Value,
)

class WDPreReference {
    private val referenceBuilder: ReferenceBuilder = ReferenceBuilder
        .newInstance()

    private val listOfProperties: MutableList<PropertyStore> = mutableListOf()

    fun build(instanceItems: InstanceItems): Reference {
        listOfProperties.forEach {
            referenceBuilder.withPropertyValue(it.property.get(instanceItems), it.value)
        }
        return referenceBuilder.build()
    }

    fun add(property: RemoteProperty, value: Value): WDPreReference {
        listOfProperties.add(PropertyStore(property, value))
        return this
    }
}
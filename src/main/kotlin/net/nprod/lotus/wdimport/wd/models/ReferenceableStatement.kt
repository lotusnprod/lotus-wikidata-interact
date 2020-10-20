package net.nprod.lotus.wdimport.wd.models

import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.Value
import net.nprod.lotus.wdimport.wd.InstanceItems

interface ReferenceableStatement {
    var property: RemoteProperty
    val preReferences: MutableList<WDPreReference>
    val overwriteable: Boolean
}

data class ReferencableValueStatement(
    override var property: RemoteProperty,
    var value: Value,
    override val preReferences: MutableList<WDPreReference> = mutableListOf(),
    override val overwriteable: Boolean=false
): ReferenceableStatement {
    fun reference(property: RemoteProperty, value: Value) = preReferences.add(WDPreReference().add(property, value))

    fun statedIn(value: Value) = preReferences.add(WDPreReference().add(InstanceItems::statedIn, value))

    constructor(property: RemoteProperty, value:String, overwriteable: Boolean=false): this(property, Datamodel.makeStringValue(value), overwriteable = overwriteable)

    companion object {
        fun monolingualValue(property: RemoteProperty, value: String) = ReferencableValueStatement(property, Datamodel.makeMonolingualTextValue(value, "en"))
    }
}

data class ReferenceableRemoteItemStatement(
    override var property: RemoteProperty,
    var value: RemoteItem,
    override val preReferences: MutableList<WDPreReference> = mutableListOf(),
    override val overwriteable: Boolean=false
): ReferenceableStatement {

    fun reference(property: RemoteProperty, value: Value) = preReferences.add(WDPreReference().add(property, value))

    fun statedIn(value: Value) = preReferences.add(WDPreReference().add(InstanceItems::statedIn, value))
}
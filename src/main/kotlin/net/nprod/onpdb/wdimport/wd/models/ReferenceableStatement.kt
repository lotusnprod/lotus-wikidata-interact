package net.nprod.onpdb.wdimport.wd.models

import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.Value
import net.nprod.onpdb.wdimport.wd.InstanceItems

sealed class ReferenceableStatement

data class ReferenceableValueStatement(
    var property: RemoteProperty,
    var value: Value,
    val preReferences: MutableList<WDPreReference> = mutableListOf()
): ReferenceableStatement() {
    fun reference(property: RemoteProperty, value: Value) = preReferences.add(WDPreReference().add(property, value))

    fun statedIn(value: Value) = preReferences.add(WDPreReference().add(InstanceItems::statedIn, value))

    constructor(property: RemoteProperty, value:String): this(property, Datamodel.makeStringValue(value))
}

data class ReferenceableRemoteItemStatement(
    var property: RemoteProperty,
    var value: RemoteItem,
    val preReferences: MutableList<WDPreReference> = mutableListOf()
): ReferenceableStatement() {

    fun reference(property: RemoteProperty, value: Value) = preReferences.add(WDPreReference().add(property, value))

    fun statedIn(value: Value) = preReferences.add(WDPreReference().add(InstanceItems::statedIn, value))
}
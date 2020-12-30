/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.Qualifier
import net.nprod.lotus.wdimport.wd.publishing.RemoteItem
import net.nprod.lotus.wdimport.wd.publishing.RemoteProperty
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.TimeValue
import org.wikidata.wdtk.datamodel.interfaces.Value
import java.time.OffsetDateTime
import kotlin.reflect.KProperty1

interface ReferencedStatement {
    var property: RemoteProperty
    val preReferences: MutableList<WDPreReference>
    val qualifiers: MutableList<Qualifier>
    val overwritable: Boolean

    fun reference(property: RemoteProperty, value: Value): Boolean =
        preReferences.add(WDPreReference().add(property, value))

    fun qualifier(property: KProperty1<InstanceItems, PropertyIdValue>, value: Value) =
        qualifiers.add(Qualifier(property, value))

    fun statedIn(value: Value): Boolean = preReferences.add(WDPreReference().add(InstanceItems::statedIn, value))
}

data class ReferencedValueStatement(
    override var property: RemoteProperty,
    var value: Value,
    override val overwritable: Boolean = false
) : ReferencedStatement {
    override val preReferences: MutableList<WDPreReference> = mutableListOf()
    override val qualifiers: MutableList<Qualifier> = mutableListOf()

    constructor(property: RemoteProperty, value: String, overwritable: Boolean = false) : this(
        property,
        Datamodel.makeStringValue(value),
        overwritable = overwritable
    )

    companion object {
        fun monolingualValue(property: RemoteProperty, value: String): ReferencedValueStatement =
            ReferencedValueStatement(property, Datamodel.makeMonolingualTextValue(value, "en"))

        fun datetimeValue(property: RemoteProperty, value: OffsetDateTime): ReferencedValueStatement =
            ReferencedValueStatement(
                property,
                Datamodel.makeTimeValue(
                    value.year.toLong(),
                    value.monthValue.toByte(),
                    value.dayOfMonth.toByte(),
                    TimeValue.CM_GREGORIAN_PRO
                )
            )
    }
}

data class ReferencedRemoteItemStatement(
    override var property: RemoteProperty,
    var value: RemoteItem,
    override val overwritable: Boolean = false
) : ReferencedStatement {
    override val preReferences: MutableList<WDPreReference> = mutableListOf()
    override val qualifiers: MutableList<Qualifier> = mutableListOf()

    /**
     * We resolve this remote item using the given instanceItems
     * it will not resolve the references or the qualifiers, this is left to the document builder
     */
    fun resolveToReferencedValueStatetement(instanceItems: InstanceItems) = ReferencedValueStatement(
        this.property,
        this.value.get(instanceItems)
    ).also {
        it.preReferences.addAll(this.preReferences)
        it.qualifiers.addAll(this.qualifiers)
    }
}

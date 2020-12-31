/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models.statements

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.models.WDPreQualifier
import net.nprod.lotus.wdimport.wd.models.WDPreReference
import net.nprod.lotus.wdimport.wd.publishing.RemoteProperty
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.Value
import kotlin.reflect.KProperty1

/**
 * A statement on which we can add qualifiers and references
 */
interface IReferencedStatement {
    /**
     * The property of the statement
     */
    var property: RemoteProperty

    /**
     * A list of pre-references that will get computed once we build the document
     */
    val preReferences: MutableList<WDPreReference>

    /**
     * A list of pre-qualifiers that will get computed once we build the document
     */
    val preQualifiers: MutableList<WDPreQualifier>

    /**
     * Means that this property can be overwritten
     */
    val overwritable: Boolean

    /**
     * Add a reference to that statement
     */
    fun reference(property: RemoteProperty, value: Value): Boolean =
        preReferences.add(WDPreReference().add(property, value))

    /**
     * Add a qualifier to that statement
     */
    fun qualifier(property: KProperty1<InstanceItems, PropertyIdValue>, value: Value) =
        preQualifiers.add(WDPreQualifier(property, value))

    /**
     * Add a statedIn reference
     */
    fun statedIn(value: Value): Boolean = preReferences.add(WDPreReference().add(InstanceItems::statedIn, value))

    /**
     * Add a reference for that statement and return the object
     */
    fun withReferenceURL(source: String?) = this.apply {
        source?.let {
            this.reference(
                InstanceItems::referenceURL,
                Datamodel.makeStringValue(source)
            )
        }
    }
}

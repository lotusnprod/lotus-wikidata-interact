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
import net.nprod.lotus.wdimport.wd.publishing.RemoteItem
import net.nprod.lotus.wdimport.wd.publishing.RemoteProperty

/**
 * A statement on which we can add qualifiers and references that takes a value that will be calculated at build time
 *
 * @param value the value of the statement that will be calculated at build time
 * @param overwritable means that this property can be overwritten
 */
data class ReferencedRemoteItemStatement(
    override var property: RemoteProperty,
    var value: RemoteItem,
    override val overwritable: Boolean = false,
) : IReferencedStatement {
    override val preReferences: MutableList<WDPreReference> = mutableListOf()
    override val preQualifiers: MutableList<WDPreQualifier> = mutableListOf()

    /**
     * We resolve this remote item using the given instanceItems
     * it will not resolve the references or the qualifiers, this is left to the document builder
     */
    fun resolveToReferencedValueStatetement(instanceItems: InstanceItems): ReferencedValueStatement =
        ReferencedValueStatement(
            this.property,
            this.value.get(instanceItems),
        ).also {
            it.preReferences.addAll(this.preReferences)
            it.preQualifiers.addAll(this.preQualifiers)
        }
}

/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models.statements

import net.nprod.lotus.wdimport.wd.models.WDPreQualifier
import net.nprod.lotus.wdimport.wd.models.WDPreReference
import net.nprod.lotus.wdimport.wd.publishing.RemoteProperty
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.TimeValue
import org.wikidata.wdtk.datamodel.interfaces.Value
import java.time.OffsetDateTime

/**
 * A statement on which we can add qualifiers and references that takes a value
 *
 * @param value the value of the statement
 */
data class ReferencedValueStatement(
    override var property: RemoteProperty,
    var value: Value,
    override val overwritable: Boolean = false,
) : IReferencedStatement {
    override val preReferences: MutableList<WDPreReference> = mutableListOf()
    override val preQualifiers: MutableList<WDPreQualifier> = mutableListOf()

    constructor(property: RemoteProperty, value: String, overwritable: Boolean = false) : this(
        property,
        Datamodel.makeStringValue(value),
        overwritable = overwritable,
    )

    companion object {
        fun monolingualValue(
            property: RemoteProperty,
            value: String,
        ): ReferencedValueStatement = ReferencedValueStatement(property, Datamodel.makeMonolingualTextValue(value, "en"))

        fun datetimeValue(
            property: RemoteProperty,
            value: OffsetDateTime,
        ): ReferencedValueStatement =
            ReferencedValueStatement(
                property,
                Datamodel.makeTimeValue(
                    value.year.toLong(),
                    value.monthValue.toByte(),
                    value.dayOfMonth.toByte(),
                    TimeValue.CM_GREGORIAN_PRO,
                ),
            )
    }
}

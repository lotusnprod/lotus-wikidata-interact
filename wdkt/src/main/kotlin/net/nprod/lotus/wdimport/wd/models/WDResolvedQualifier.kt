/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.Value

/**
 * A qualifier that has been resolved for this instance.
 *
 * @param property property of the qualifier
 * @param value value of the qualifier
 */
data class WDResolvedQualifier(
    val property: PropertyIdValue,
    val value: Value,
) {
    companion object {
        /**
         * Resolve the qualifier
         */
        fun fromQualifier(
            prequalifier: WDPreQualifier,
            instanceItems: InstanceItems,
        ): WDResolvedQualifier = WDResolvedQualifier(prequalifier.property.get(instanceItems), prequalifier.value)
    }
}

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
import kotlin.reflect.KProperty1

/**
 * A qualifier that can be added to a statement.
 */
data class WDPreQualifier(
    val property: KProperty1<InstanceItems, PropertyIdValue>,
    val value: Value
)

/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd

import net.nprod.lotus.wdimport.wd.publishing.RemoteItem
import net.nprod.lotus.wdimport.wd.publishing.RemoteProperty
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

/**
 * Resolve the given element according to the instanceItem of that item
 */
interface Resolver {
    val instanceItems: InstanceItems

    fun resolve(item: RemoteItem): ItemIdValue = item.get(instanceItems)

    fun resolve(item: RemoteProperty): PropertyIdValue = item.get(instanceItems)
}

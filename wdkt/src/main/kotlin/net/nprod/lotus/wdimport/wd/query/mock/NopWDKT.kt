/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.query.mock

import net.nprod.lotus.wdimport.wd.query.IWDKT
import net.nprod.lotus.wdimport.wd.query.QueryActionResponse
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

/**
 * A class that implement IWDKT but does nothing, this is useful when
 * you want to test against an hypothetical empty WikiData instance
 */
class NopWDKT : IWDKT {
    @Suppress("EmptyFunctionBlock")
    override fun close() {
    }

    override fun searchDOI(doi: String): QueryActionResponse? = null

    override fun searchForPropertyValue(
        property: PropertyIdValue,
        value: String,
    ): QueryActionResponse? = null
}

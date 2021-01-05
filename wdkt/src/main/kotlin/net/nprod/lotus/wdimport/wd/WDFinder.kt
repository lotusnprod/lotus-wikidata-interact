/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd

import io.ktor.util.KtorExperimentalAPI
import net.nprod.konnector.crossref.CrossRefConnector
import net.nprod.konnector.crossref.OfficialCrossRefAPI
import net.nprod.lotus.wdimport.wd.query.IWDKT
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import kotlin.time.ExperimentalTime

class WDFinder(val wdkt: IWDKT, val sparql: ISparql) {
    @ExperimentalTime
    @KtorExperimentalAPI
    val crossRefConnector: CrossRefConnector by lazy {
        CrossRefConnector(OfficialCrossRefAPI())
    }

    fun close() = wdkt.close()
}

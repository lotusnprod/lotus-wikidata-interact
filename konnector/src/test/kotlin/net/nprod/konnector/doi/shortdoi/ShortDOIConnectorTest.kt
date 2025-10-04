/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.doi.shortdoi

import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class ShortDOIConnectorTest {
    @Test
    fun shorten() {
        val result =
            ShortDOIConnector(OfficialShortDOIAPI())
                .shorten("10.1002/(SICI)1097-0258(19980815/30)17:15/16<1661::AID-SIM968>3.0.CO;2-2")
        assert(result.shortDOI == "10/aabbe")
    }
}

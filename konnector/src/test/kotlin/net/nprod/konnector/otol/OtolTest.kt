/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.otol

import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class OtolTest {
    private var connector = OtolConnector(OfficialOtolAPI())

    @Test
    fun about() {
        val about = connector.taxonomy.about()
        assert(about.name == "ott")
    }

    @Test
    fun `basic taxon info`() {
        val taxInfo = connector.taxonomy.taxonInfo(515698)
        assert(taxInfo.unique_name == "Barnadesia")
    }

    @Test
    fun `taxon info with lineage`() {
        val taxInfo = connector.taxonomy.taxonInfo(515698, includeLineage = true)
        assert(taxInfo.lineage?.any { it.name == "Pentapetalae" } ?: false)
    }

    @Test
    fun `match name exact`() {
        val matchedName = connector.Tnrs().matchNames(listOf("Aster", "Symphyotrichum", "Barnadesia"))
        assert(matchedName.results.size == 3)
    }

    @Test
    fun `match name fuzzy`() {
        val matchedName =
            connector.Tnrs().matchNames(listOf("Asteyr", "Simphyotrichum", "Barnadosia"), approximateMatching = true)
        println(matchedName)
        assert(matchedName.results.size == 3)
    }
}

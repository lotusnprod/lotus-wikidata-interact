/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.globalnames.verify

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

const val MINIMAL_NUMBER_OF_SOURCES = 100

@ExperimentalTime
@Tag("integration")
internal class GlobalNamesVerifyConnectorTest {
    private var connector = GlobalNamesVerifyConnector(OfficialGlobalNamesVerifyAPI())

    @Test
    fun ping() {
        assert(connector.ping())
    }

    @Test
    fun version() {
        val version = connector.version()
        assert(version.version.isNotEmpty())
        assert(version.build.startsWith("20"))
    }

    @Test
    fun dataSources() {
        val sources = connector.dataSources()
        assert(sources.size > MINIMAL_NUMBER_OF_SOURCES)
    }

    @Test
    fun dataSource() {
        val source = connector.dataSource(1)
        assert(source.title == "Catalogue of Life")
    }

    @Test
    fun verifications() {
        val source: Verification =
            connector.verifications(
                VerificationQuery(
                    nameStrings =
                        listOf(
                            "Pomatomus soltator",
                            "Bubu bubo (Linnaeus, 1758)", // The error here is on purpose, so we get no preferred result
                        ),
                    preferredSources = listOf(1, 12, 169),
                    withVernaculars = false,
                ),
            )
        assert(source.names.size == 2)
        assert(
            source.names
                .first {
                    it.name == "Pomatomus soltator"
                }.bestResult.currentName == "Pomatomus saltatrix (Linnaeus, 1766)",
        )
    }
}

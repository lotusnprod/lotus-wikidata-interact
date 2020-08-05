package wd

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import wd.sparql.WDSparql
import wd.sparql.findOrganismByTaxon

internal class WDOrganismTest {
    lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql()
    }

    @Test
    fun findOrganismByTaxon() {
        val expected = mapOf(
            "Phalaris arundinacea" to listOf("Q157419"),
            "Lactuca virosa" to listOf("Q578927")
        )

        val result = wdSparql.findOrganismByTaxon(expected.keys.toList())
        assertEquals(2, result.size)
        result.forEach {
            assertEquals(expected[it.key], it.value)
        }
    }
}
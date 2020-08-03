package wd

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

internal class WDOrganismTest {
    lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql()
    }

    @Test
    fun findOrganismByTaxon() {
        val wdOrganism = WDOrganism(wdSparql)
        val expected = mapOf(
            "Phalaris arundinacea" to listOf("Q157419"),
            "Lactuca virosa" to listOf("Q578927")
        )

        val result = wdOrganism.findOrganismByTaxon(expected.keys.toList())
        assertEquals(2, result.size)
        result.forEach {
            assertEquals(expected[it.key], it.value)
        }
    }
}
package wd

import net.nprod.onpdb.wdimport.wd.MainInstanceItems
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql
import net.nprod.onpdb.wdimport.wd.sparql.findTaxonByName

internal class WDTaxonTest {
    lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql(MainInstanceItems)
    }

    @Test
    fun findOrganismByTaxon() {
        val expected = mapOf(
            "Phalaris arundinacea" to listOf("Q157419"),
            "Lactuca virosa" to listOf("Q578927")
        )

        val result = wdSparql.findTaxonByName(expected.keys.toList())
        assertEquals(2, result.size)
        result.forEach {
            assertEquals(expected[it.key], it.value)
        }
    }
}
package wd

import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import net.nprod.lotus.wdimport.wd.sparql.findTaxonByName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class WDTaxonTest {
    private lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql(MainInstanceItems)
    }

    @Test
    fun findOrganismByTaxon() {
        val expected =
            mapOf(
                "Phalaris arundinacea" to listOf("Q157419"),
                "Lactuca virosa" to listOf("Q578927"),
            )

        val result = wdSparql.findTaxonByName(expected.keys.toList())
        assertEquals(2, result.size)
        result.forEach {
            assertEquals(expected[it.key], it.value)
        }
    }
}

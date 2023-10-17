package wd

import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.sparql.WDSparql
import net.nprod.lotus.wdimport.wd.sparql.findReferencesByDOI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class WDArticleTest {
    private lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql(MainInstanceItems)
    }

    @Test
    fun `Find references by DOI`() {
        val dois =
            mapOf(
                "10.1021/ACS.JMEDCHEM.5B01009" to listOf("Q26778522"),
                "10.3389/FPLS.2019.01329" to listOf("Q91218352"),
            )

        val result = wdSparql.findReferencesByDOI(dois.keys.toList())
        result.forEach {
            assertEquals(dois[it.key], it.value)
        }
    }
}

package wd

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import wd.sparql.WDSparql
import wd.sparql.findCompoundsByInChIKey

class WDCompoundSearchTest {
    lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql()
    }

    @Test
    fun findCompoundsByInChIKey() {
        val inChIs = mapOf(
            "VFLDPWHFBUODDF-FCXRPNKRSA-N" to listOf("Q312266"),
            "REFJWTPEDVJJIY-UHFFFAOYSA-N" to listOf("Q409478")
        )
        val result = wdSparql.findCompoundsByInChIKey(inChIs.keys.toList())
        assertEquals(2, result.size)
        result.forEach {
            assertEquals(inChIs[it.key], it.value)
        }
    }

    @Test
    fun findCompoundsByInChIKeyChunked() {
        val inChIs = mapOf(
            "VFLDPWHFBUODDF-FCXRPNKRSA-N" to listOf("Q312266"),
            "REFJWTPEDVJJIY-UHFFFAOYSA-N" to listOf("Q409478"),
            "DMULVCHRPCFFGV-UHFFFAOYSA-N" to listOf("Q407217"),
            "RYYVLZVUVIJVGH-UHFFFAOYSA-N" to listOf("Q60235")

        )
        val result = wdSparql.findCompoundsByInChIKey(inChIs.keys.toList(), chunkSize = 2)
        assertEquals(4, result.size)
        result.forEach {
            assertEquals(inChIs[it.key], it.value)
        }
    }
}
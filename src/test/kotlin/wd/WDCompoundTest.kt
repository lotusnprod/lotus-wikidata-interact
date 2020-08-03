package wd

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WDCompoundTest {
    lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql()
    }

    @Test
    fun findCompoundByInChIKey() {
        val wdCompound = WDCompound(wdSparql)
        val inChIKey = "VFLDPWHFBUODDF-FCXRPNKRSA-N"
        val expected = listOf("Q312266")
        val result = wdCompound.findCompoundByInChIKey(inChIKey)
        assertEquals(1, result.size)
        assertEquals(expected, result[inChIKey])
    }

    @Test
    fun findCompoundsByInChIKey() {
        val wdCompound = WDCompound(wdSparql)
        val inChIs = mapOf(
            "VFLDPWHFBUODDF-FCXRPNKRSA-N" to listOf("Q312266"),
            "REFJWTPEDVJJIY-UHFFFAOYSA-N" to listOf("Q409478")
        )
        val result = wdCompound.findCompoundsByInChIKey(inChIs.keys.toList())
        assertEquals(2, result.size)
        result.forEach {
            assertEquals(inChIs[it.key], it.value)
        }
    }

    @Test
    fun findCompoundsByInChIKeyChunked() {
        val wdCompound = WDCompound(wdSparql)
        val inChIs = mapOf(
            "VFLDPWHFBUODDF-FCXRPNKRSA-N" to listOf("Q312266"),
            "REFJWTPEDVJJIY-UHFFFAOYSA-N" to listOf("Q409478"),
            "DMULVCHRPCFFGV-UHFFFAOYSA-N" to listOf("Q407217"),
            "RYYVLZVUVIJVGH-UHFFFAOYSA-N" to listOf("Q60235")

        )
        val result = wdCompound.findCompoundsByInChIKey(inChIs.keys.toList(), chunkSize = 2)
        assertEquals(4, result.size)
        result.forEach {
            assertEquals(inChIs[it.key], it.value)
        }
    }
}
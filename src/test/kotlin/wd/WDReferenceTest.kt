package wd

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class WDReferenceTest {
    lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql()
    }

    @Test
    fun findReferencesByDOI() {
        val wdReference = WDReference(wdSparql)
        val dois = mapOf("10.1021/ACS.JMEDCHEM.5B01009" to listOf("Q26778522"),
            "10.3389/FPLS.2019.01329" to listOf("Q91218352"))

        val result = wdReference.findReferencesByDOI(dois.keys.toList())
        result.forEach {
            assertEquals(dois[it.key], it.value)
        }
    }
}
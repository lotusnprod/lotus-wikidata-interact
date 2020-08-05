package wd

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import wd.sparql.WDSparql
import wd.sparql.findReferencesByDOI

internal class WDReferenceTest {
    lateinit var wdSparql: WDSparql

    @BeforeEach
    fun setUp() {
        wdSparql = WDSparql()
    }

    @Test
    fun findReferencesByDOI() {
        val dois = mapOf("10.1021/ACS.JMEDCHEM.5B01009" to listOf("Q26778522"),
            "10.3389/FPLS.2019.01329" to listOf("Q91218352"))

        val result = wdSparql.findReferencesByDOI(dois.keys.toList())
        result.forEach {
            assertEquals(dois[it.key], it.value)
        }
    }
}
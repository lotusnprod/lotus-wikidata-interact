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
    fun findReferenceByDOI() {
        val wdReference = WDReference(wdSparql)
        val doi = "10.1021/ACS.JMEDCHEM.5B01009"
        assertEquals(listOf("Q26778522"), wdReference.findReferenceByDOI(doi)[doi])
    }

    @Test
    fun findReferencesByDOI() {
        val wdReference = WDReference(wdSparql)
        val dois = mapOf("10.1021/ACS.JMEDCHEM.5B01009" to listOf("Q26778522"),
            "10.3389/FPLS.2019.01329" to listOf("Q91218352"))
        dois.forEach {
            assertEquals(it.value, wdReference.findReferenceByDOI(it.key)[it.key])
        }
    }
}
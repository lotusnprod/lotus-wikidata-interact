package net.nprod.lotus.wdimport.wd.query

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class WDKTTest {

    @Test
    fun `make sure we can have weird DOIs`() {
        val instance = WDKT()
        assert(instance.searchDOI("10.1021/NP049900+").query.searchinfo.totalhits >= 1)
    }
}
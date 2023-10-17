package net.nprod.lotus.wdimport.wd.query

import net.nprod.lotus.wdimport.wd.MainInstanceItems
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class WDKTTest {
    private lateinit var instance: WDKT

    @BeforeEach
    fun setup() {
        instance = WDKT()
    }

    @Test
    fun `make sure we can have weird DOIs`() {
        assert(
            instance.searchForPropertyValue(
                MainInstanceItems.doi,
                "10.1021/NP049900+",
            ).query.searchinfo.totalhits >= 1,
        )
    }

    @Test
    fun `make sure we can find ISSNs`() {
        assert(
            instance.searchForPropertyValue(
                MainInstanceItems.issn,
                "0163-3864",
            ).query.searchinfo.totalhits >= 1,
        )
    }
}

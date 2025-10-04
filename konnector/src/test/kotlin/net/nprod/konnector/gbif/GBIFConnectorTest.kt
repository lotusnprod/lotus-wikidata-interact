package net.nprod.konnector.gbif

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class GBIFConnectorTest {
    private var connector = GBIFConnector(OfficialGBIFAPI())

    @Test
    fun `taxon occurrence search`() {
        val output =
            connector.occurenceOfTaxon(
                "1",
                offset = 42,
            )
        assertEquals(42, output.offset)
    }

    @Test
    fun `taxon key search`() {
        val output = connector.taxonkeyByName("Curcuma")
        println(output)
        assertEquals(2757518, output.genusKey)
    }
}

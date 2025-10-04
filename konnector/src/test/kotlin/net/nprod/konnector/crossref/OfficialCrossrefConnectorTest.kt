package net.nprod.konnector.crossref

import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class OfficialCrossrefConnectorTest {
    private var connector = CrossRefConnector(OfficialCrossRefAPI())

    @Test
    fun `basic search`() {
        val output: WorksResponse =
            connector.works(
                query = "bisson can invalid bioactives medicinal 10.1021/acs.jmedchem.5b01009",
            )
        assert(
            output.message
                ?.items
                ?.get(0)
                ?.doi == "10.1021/acs.jmedchem.5b01009",
        )
    }

    @Test
    fun `entries with null values in dates`() {
        val output = connector.workFromDOI("10.1021/NP060633C.S001")
        assert(output.status == "ok")
    }
}

package net.nprod.konnector.gnfinder

import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.util.concurrent.Executors

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfSystemProperty(named = "gnfinderTest", matches = "true")
internal class GNFinderClientTest {
    val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
    val client = GNFinderClient("localhost:8778", dispatcher)

    @Test
    fun ping() {
        assert(client.ping() == "pong")
    }

    @Test
    fun ver() {
        assert(client.ver().startsWith("v"))
    }

    @Test
    fun findNames() {
        assert("Curcuma longa" in client.findNames("The source of the compound, Curcuma longa, is a plant."))
    }

    @Test
    fun findPositions() {
        val text = "The source of the compound, Curcuma longa, is a plant."
        val species = "Curcuma longa"
        val output = client.findNamesToStructured(text, verification = false)
        assert(output.names?.first()?.offsetStart == text.indexOf(species))
    }

    @Test
    fun findNamesWithSources() {
        assert(
            "Plantae|Tracheophyta|Liliopsida|Zingiberales|Zingiberaceae|Curcuma|Curcuma longa" in
                client.findNames(
                    "The source of the compound, Curcuma longa, is a plant.",
                    sources = (1..182),
                    verification = true,
                ),
        )
    }

    @AfterAll
    internal fun done() {
        client.close()
        dispatcher.close()
    }
}

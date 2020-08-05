import org.apache.logging.log4j.LogManager
import wd.*
import wd.models.WDCompound


fun main(args: Array<String>) {
    val logger = LogManager.getLogger("main")
    logger.info("Playing with Wikidata Toolkit")

    /*logger.info("Initializing toolkit")
    val wikibaseDataFetcher = WikibaseDataFetcher(
        BasicApiConnection.getWikidataApiConnection(),
        Datamodel.SITE_WIKIDATA
    )*/

    /*logger.info("Fetching data for curcumin")
    val curcumin = wikibaseDataFetcher.getEntityDocument("Q312266")
    logger.info("Good for your curry!")*/

    /*logger.info("Initializing the Sparql stuff")
    val wdSparql = WDSparql()
    val wdCompound = WDCompoundSearch(wdSparql)*/

    val publisher = WDPublisher()
    publisher.connect()
    val compound = WDCompound("fauxine", "INCHIFAKEY", "INCHIFAKE", "CCCOOOCCC", "123", "C6O3")
    publisher.publish(compound.document(TestInstanceItems), "Creating a new fake compound")
    publisher.disconnect()
}


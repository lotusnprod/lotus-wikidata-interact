import org.apache.logging.log4j.LogManager
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import wd.*
import wd.models.WDCompound
import wd.models.WDTaxon


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

    val publisher = WDPublisher(TestInstanceItems)
    publisher.connect()

    val genus = WDTaxon(name = "Fauxa", parentTaxon = null , taxonName = "Fauxa", taxonRank = TestInstanceItems.genus)
    publisher.publish(genus, "Creating a new fake Genus")
    val species = WDTaxon(name = "Fauxa longata", parentTaxon = genus.id , taxonName = "Fauxa longata", taxonRank = TestInstanceItems.species)
    publisher.publish(species, "Creating a new fake Species")

    val compound = WDCompound("fauxine", "INCHIFAKEY", "INCHIFAKE", "CCCOOOCCC", "123", "C6O3")
    compound.addNaturalProductOfTaxon(species)
    val compoundId = publisher.publish(compound, "Creating a new fake compound")


    publisher.disconnect()
}


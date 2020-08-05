package net.nprod.onpdb.wdimport

import org.apache.logging.log4j.LogManager
import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.MainInstanceItems
import net.nprod.onpdb.wdimport.wd.TestInstanceItems
import net.nprod.onpdb.wdimport.wd.WDPublisher
import net.nprod.onpdb.wdimport.wd.models.WDTaxon
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql


fun main(args: Array<String>) {
    val logger = LogManager.getLogger("net.nprod.onpdb.wdimport.main")
    logger.info("Playing with Wikidata Toolkit")

    val instanceItems = TestInstanceItems

    logger.info("Initializing toolkit")
    /*val wikibaseDataFetcher = WikibaseDataFetcher(
        BasicApiConnection.getTestWikidataApiConnection(), // TODO put that in instance as well
        "http://test.wikidata.org/entity/"
    )*/
/*
    logger.info("Fetching data for something")
    val thing = wikibaseDataFetcher.getEntityDocument("Q212578")
    println(thing)*/

    val wdSparql = WDSparql(MainInstanceItems) // TODO: For tests we use the official…
    val publisher = WDPublisher(instanceItems)

    publisher.connect()

    val genus = WDTaxon(
        name = "Curcuma",
        parentTaxon = null,
        taxonName = "Curcuma",
        taxonRank = InstanceItems::genus
    ).tryToFind(wdSparql, instanceItems)
/*
    publisher.publish(genus, "Creating a new fake Genus")

    val species = WDTaxon(
        name = "Fauxa longata",
        parentTaxon = genus.id,
        taxonName = "Fauxa longata",
        taxonRank = InstanceItems::species
    )

    publisher.publish(species, "Creating a new fake Species")

    val article = WDArticle(
        name = "Fake article",
        title = "Fake article",
        doi = "42.18042"
    )
    publisher.publish(article, "Creating a new article")

    val compound = WDCompound(
        name = "fauxine",
        inChIKey = "INCHIFAKEY",
        inChI = "INCHIFAKE",
        isomericSMILES = "CCCOOOCCC",
        pcId = "123",
        chemicalFormula = "C6O3"
    ) {
        naturalProductOfTaxon(species) {
            statedIn(article.id)
        }
    }

    publisher.publish(compound, "Creating a new compound")
*/
    publisher.disconnect()
}
package wd

import org.apache.logging.log4j.LogManager
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.helpers.Datamodel.makePropertyIdValue
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.Statement
import org.wikidata.wdtk.util.WebResourceFetcherImpl
import org.wikidata.wdtk.wikibaseapi.ApiConnection
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection
import org.wikidata.wdtk.wikibaseapi.WikibaseDataEditor
import java.net.ConnectException


class EnvironmentVariableError(message: String) : Exception(message)
class InternalError(message: String) : Exception(message)

class WDPublisher {
    val userAgent = "Wikidata Toolkit EditOnlineDataExample"
    val siteIri = "http://www.test.wikidata.org/entity/"
    val sitePageURL = "https://test.wikidata.org/w/index.php?title="
    val logger = LogManager.getLogger(this::class.java)
    private var user: String? = null
    private var password: String? = null
    private var connection: ApiConnection? = null
    private var editor: WikibaseDataEditor? = null

    init {
        user = System.getenv("WIKIDATA_USER")
            ?: throw EnvironmentVariableError("Missing environment variable WIKIDATA_USER")
        password = System.getenv("WIKIDATA_PASSWORD")
            ?: throw EnvironmentVariableError("Missing environment variable WIKIDATA_PASSWORD")
    }

    fun connect() {
        connection = BasicApiConnection.getTestWikidataApiConnection()
        connection?.login(user, password) ?:
                throw ConnectException("Impossible to connect to the WikiData instance.")
        editor = WikibaseDataEditor(connection, siteIri)
        require(connection?.isLoggedIn ?: false) { "Impossible to login in the instance"}
    }

    fun disconnect() = connection?.logout()

    fun publish() {
        require(connection != null) { "You need to connect first" }
        require(editor != null) { "The editor should exist, you connection likely failed and we didn't catch that" }
        WebResourceFetcherImpl
            .setUserAgent(userAgent)

        val noid = ItemIdValue.NULL // used when creating new items

        val property1 = makePropertyIdValue("P95458", siteIri)
        val property2 = makePropertyIdValue("P95459", siteIri)

        val statement1: Statement = StatementBuilder
            .forSubjectAndProperty(noid, property1)
            .withValue(Datamodel.makeStringValue("String value 1")).build()
        val statement2: Statement = StatementBuilder
            .forSubjectAndProperty(noid, property1)
            .withValue(
                Datamodel
                    .makeStringValue("Item created with Kotlin using the Wikidata Toolkit")
            )
            .build()
        val statement3: Statement = StatementBuilder
            .forSubjectAndProperty(noid, property2)
            .withValue(Datamodel.makeStringValue("Preparing a bot for the Natural products project")).build()

        val itemDocument = ItemDocumentBuilder.forItemId(noid)
            .withLabel("Wikidata Toolkit test", "en")
            .withStatement(statement1).withStatement(statement2)
            .withStatement(statement3).build()

        val newItemDocument: ItemDocument = editor?.createItemDocument(
            itemDocument,
            "Test document", null
        ) ?: throw InternalError("There is no editor anymore")

        val newItemId = newItemDocument.entityId
        logger.info("Successfully created the item: ${newItemId.id}")
        logger.info("you can access it at $sitePageURL${newItemId.id}")
    }
}
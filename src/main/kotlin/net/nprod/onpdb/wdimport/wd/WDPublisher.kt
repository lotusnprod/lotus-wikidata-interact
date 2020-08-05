package net.nprod.onpdb.wdimport.wd

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder
import org.wikidata.wdtk.datamodel.helpers.ReferenceBuilder
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder
import org.wikidata.wdtk.datamodel.interfaces.*
import org.wikidata.wdtk.util.WebResourceFetcherImpl
import org.wikidata.wdtk.wikibaseapi.ApiConnection
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection
import org.wikidata.wdtk.wikibaseapi.WikibaseDataEditor
import net.nprod.onpdb.wdimport.wd.models.Publishable
import net.nprod.onpdb.wdimport.wd.models.ReferenceableRemoteItemStatement
import net.nprod.onpdb.wdimport.wd.models.ReferenceableValueStatement
import java.net.ConnectException

class EnvironmentVariableError(message: String) : Exception(message)
class InternalError(message: String) : Exception(message)


class WDPublisher(override val instanceItems: InstanceItems) : Resolver {
    private val userAgent = "Wikidata Toolkit EditOnlineDataExample"
    val logger: Logger = LogManager.getLogger(this::class.java)
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
        connection?.login(user, password) ?: throw ConnectException("Impossible to connect to the WikiData instance.")
        editor = WikibaseDataEditor(connection, instanceItems.siteIri)
        require(connection?.isLoggedIn ?: false) { "Impossible to login in the instance" }
    }

    fun disconnect() = connection?.logout()

    fun publish(publishable: Publishable, summary: String): ItemIdValue {
        require(connection != null) { "You need to connect first" }
        require(editor != null) { "The editor should exist, you connection likely failed and we didn't catch that" }
        WebResourceFetcherImpl
            .setUserAgent(userAgent)

        val newItemDocument: ItemDocument = editor?.createItemDocument(
            publishable.document(instanceItems),
            summary, null
        ) ?: throw InternalError("There is no editor anymore")

        val newItemId = newItemDocument.entityId
        logger.info("Successfully created the item: ${newItemId.id}")
        logger.info("you can access it at ${instanceItems.sitePageIri}${newItemId.id}")
        publishable.published(newItemId)
        return newItemId
    }
}

/**
 * Type safe builder or DSL
 */

fun newReference(f: (ReferenceBuilder) -> Unit): Reference {
    val reference = ReferenceBuilder.newInstance()
    reference.apply(f)
    return reference.build()
}

fun ReferenceBuilder.propertyValue(property: PropertyIdValue, value: String) =
    this.propertyValue(property, Datamodel.makeStringValue(value))

fun ReferenceBuilder.propertyValue(property: PropertyIdValue, value: Value) {
    this.withPropertyValue(
        property,
        value
    )
}

fun StatementBuilder.reference(reference: Reference) {
    this.reference(reference)
}

fun newDocument(name: String, f: ItemDocumentBuilder.() -> Unit): ItemDocument {
    val builder = ItemDocumentBuilder.forItemId(ItemIdValue.NULL)
        .withLabel(name, "en")

    builder.apply(f)

    return builder.build()
}

fun ItemDocumentBuilder.statement(statement: Statement) {
    this.withStatement(statement)
}


fun ItemDocumentBuilder.statement(property: PropertyIdValue, value: String, f: (StatementBuilder) -> Unit = {}) =
    this.withStatement(newStatement(property, Datamodel.makeStringValue(value), f))

fun ItemDocumentBuilder.statement(property: PropertyIdValue, value: Value, f: (StatementBuilder) -> Unit = {}) =
    this.withStatement(newStatement(property, value, f))

fun ItemDocumentBuilder.statement(referenceableStatement: ReferenceableValueStatement, instanceItems: InstanceItems) {
    val statement = newStatement(
        referenceableStatement.property.get(instanceItems),
        referenceableStatement.value
    ) { statementBuilder ->
        referenceableStatement.preReferences.forEach {
            statementBuilder.withReference(it.build(instanceItems))
        }
    }
    this.withStatement(statement)
}

fun ItemDocumentBuilder.statement(
    referenceableStatement: ReferenceableRemoteItemStatement,
    instanceItems: InstanceItems
) =
    this.statement(
        ReferenceableValueStatement(
            referenceableStatement.property,
            referenceableStatement.value.get(instanceItems),
            referenceableStatement.preReferences
        ),
        instanceItems
    )

fun newStatement(property: PropertyIdValue, value: Value, f: (StatementBuilder) -> Unit = {}): Statement {
    val statement = StatementBuilder.forSubjectAndProperty(ItemIdValue.NULL, property)
        .withValue(value)
        .apply(f)
    return statement.build()
}

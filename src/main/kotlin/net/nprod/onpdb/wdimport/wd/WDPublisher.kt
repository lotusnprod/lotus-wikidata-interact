package net.nprod.onpdb.wdimport.wd

import net.nprod.onpdb.wdimport.wd.models.Publishable
import net.nprod.onpdb.wdimport.wd.models.ReferenceableRemoteItemStatement
import net.nprod.onpdb.wdimport.wd.models.ReferenceableValueStatement
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wikidata.wdtk.datamodel.helpers.*
import org.wikidata.wdtk.datamodel.interfaces.*
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue.DT_STRING
import org.wikidata.wdtk.util.WebResourceFetcherImpl
import org.wikidata.wdtk.wikibaseapi.ApiConnection
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection
import org.wikidata.wdtk.wikibaseapi.WikibaseDataEditor
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException
import java.net.ConnectException

class EnvironmentVariableError(message: String) : Exception(message)
class InternalError(message: String) : Exception(message)


class WDPublisher(override val instanceItems: InstanceItems) : Resolver, Publisher {
    private val userAgent = "Wikidata Toolkit EditOnlineDataExample"
    private val logger: Logger = LogManager.getLogger(this::class.java)
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

    override fun connect() {
        connection = BasicApiConnection.getTestWikidataApiConnection()
        connection?.login(user, password) ?: throw ConnectException("Impossible to connect to the WikiData instance.")
        editor = WikibaseDataEditor(connection, instanceItems.siteIri)
        require(connection?.isLoggedIn ?: false) { "Impossible to login in the instance" }
    }

    override fun disconnect() {
        connection?.logout()
    }

    override fun newProperty(name: String, description: String): PropertyIdValue {
        logger.info("Building a property with $name $description")
        val doc = PropertyDocumentBuilder.forPropertyIdAndDatatype(PropertyIdValue.NULL, DT_STRING)
            .withLabel(Datamodel.makeMonolingualTextValue(name, "en"))
            .withDescription(Datamodel.makeMonolingualTextValue(description, "en")).build()
        try {
            return try {
                val o = editor?.createPropertyDocument(
                    doc,
                    "Added a new property for ONPDB", listOf()
                ) ?: throw Exception("Sorry you can't create a property without connecting first.")
                o.entityId
            } catch (e: IllegalArgumentException) {
                logger.error("There is a weird bug here, it still creates it, but isn't happy anyway")
                logger.error("Restarting it…")
                newProperty(name, description)
            }

        } catch (e: MediaWikiApiErrorException) {
            if ("already has label" in e.errorMessage) {
                logger.error("This property already exists: ${e.errorMessage}")
                return Datamodel.makePropertyIdValue(
                    e.errorMessage.subSequence(
                        e.errorMessage.indexOf(':') + 1,
                        e.errorMessage.indexOf('|')
                    ).toString(), ""
                )
            } else {
                throw e
            }
        }
    }

    override fun publish(publishable: Publishable, summary: String): ItemIdValue {
        require(connection != null) { "You need to connect first" }
        require(editor != null) { "The editor should exist, you connection likely failed and we didn't catch that" }
        WebResourceFetcherImpl
            .setUserAgent(userAgent)

        if (!publishable.published) {

            val newItemDocument: ItemDocument = editor?.createItemDocument(
                publishable.document(instanceItems),
                summary, null
            ) ?: throw InternalError("There is no editor anymore")

            val itemId = newItemDocument.entityId
            logger.info("$summary: ${itemId.id}")
            logger.info("you can access it at ${instanceItems.sitePageIri}${itemId.id}")
            publishable.published(itemId)
        } else {

            editor?.updateStatements(
                publishable.id, publishable.listOfStatementsForUpdate(instanceItems),
                listOf(), "Updating the statements", null
            )
        }

        return publishable.id
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

fun newDocument(name: String, id: ItemIdValue? = null, f: ItemDocumentBuilder.() -> Unit): ItemDocument {
    val builder = ItemDocumentBuilder.forItemId(id ?: ItemIdValue.NULL)
        .withLabel(name, "en")

    builder.apply(f)

    return builder.build()
}

fun ItemDocumentBuilder.statement(
    subject: ItemIdValue? = null,
    property: PropertyIdValue,
    value: String,
    f: (StatementBuilder) -> Unit = {}
) =
    this.withStatement(newStatement(property, subject, Datamodel.makeStringValue(value), f))

fun ItemDocumentBuilder.statement(
    subject: ItemIdValue? = null,
    property: PropertyIdValue,
    value: Value,
    f: (StatementBuilder) -> Unit = {}
) =
    this.withStatement(newStatement(property, subject, value, f))

fun ItemDocumentBuilder.statement(
    subject: ItemIdValue? = null,
    referenceableStatement: ReferenceableValueStatement,
    instanceItems: InstanceItems
) {
    val statement = newStatement(
        referenceableStatement.property.get(instanceItems),
        subject,
        referenceableStatement.value
    ) { statementBuilder ->
        referenceableStatement.preReferences.forEach {
            statementBuilder.withReference(it.build(instanceItems))
        }
    }
    this.withStatement(statement)
}

fun ItemDocumentBuilder.statement(
    subject: ItemIdValue?,
    referenceableStatement: ReferenceableRemoteItemStatement,
    instanceItems: InstanceItems
) =
    this.statement(
        subject,
        ReferenceableValueStatement(
            referenceableStatement.property,
            referenceableStatement.value.get(instanceItems),
            referenceableStatement.preReferences
        ),
        instanceItems
    )

fun newStatement(
    property: PropertyIdValue,
    subject: ItemIdValue? = null,
    value: Value,
    f: (StatementBuilder) -> Unit = {}
): Statement {
    val statement = StatementBuilder.forSubjectAndProperty(subject ?: ItemIdValue.NULL, property)
        .withValue(value)
        .apply(f)
    return statement.build()
}

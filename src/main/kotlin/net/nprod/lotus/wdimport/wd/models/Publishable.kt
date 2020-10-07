package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.newDocument
import net.nprod.lotus.wdimport.wd.newStatement
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import net.nprod.lotus.wdimport.wd.statement
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.*
import kotlin.reflect.KProperty1

class ElementNotPublishedError(msg: String) : Exception(msg)

typealias RemoteItem = KProperty1<InstanceItems, ItemIdValue>
typealias RemoteProperty = KProperty1<InstanceItems, PropertyIdValue>

/**
 * A publishable is a document and its properties.
 * This allows to create documents and update them.
 */
abstract class Publishable {
    private var _id: ItemIdValue? = null

    abstract var name: String
    abstract var type: RemoteItem

    var published: Boolean = false

    val id: ItemIdValue
        get() = _id
            ?: throw ElementNotPublishedError("This element has not been published yet or failed to get published.")

    val preStatements: MutableList<ReferenceableStatement> = mutableListOf()

    fun published(id: ItemIdValue) {
        _id = id
        published = true
    }

    abstract fun dataStatements(): List<ReferenceableStatement>

    /**
     * Generate a document for that entry.
     * It will use the internal id if it exists already
     */
    fun document(instanceItems: InstanceItems, subject: ItemIdValue? = null): ItemDocument {
        preStatements.addAll(dataStatements())

        // We are limited to names < 250 characters
        val legalName = if (name.length<250) { name } else { "" }

        return newDocument(legalName, subject ?: _id) {
            statement(subject ?: _id, instanceItems.instanceOf, type.get(instanceItems))

            // We construct the statements according to this instanceItems value
            preStatements.forEach { refStat ->
                when (refStat) {
                    is ReferencableValueStatement -> statement(subject ?: _id, refStat, instanceItems)
                    is ReferenceableRemoteItemStatement -> statement(subject ?: _id, refStat, instanceItems)
                }
            }
            preStatements.clear()
        }
    }

    /**
     * Generate statements for updating
     */
    fun listOfStatementsForUpdate(instanceItems: InstanceItems): List<Statement> {
        return preStatements.map { referenceableStatement ->
            when (referenceableStatement) {
                is ReferencableValueStatement -> newStatement(
                    referenceableStatement.property.get(instanceItems),
                    id,
                    referenceableStatement.value
                ) { statementBuilder ->
                    referenceableStatement.preReferences.forEach {
                        statementBuilder.withReference(it.build(instanceItems))
                    }
                }
                is ReferenceableRemoteItemStatement -> newStatement(
                    referenceableStatement.property.get(instanceItems),
                    id,
                    ReferencableValueStatement(
                        referenceableStatement.property,
                        referenceableStatement.value.get(instanceItems),
                        referenceableStatement.preReferences
                    ).value
                ) { statementBuilder ->
                    referenceableStatement.preReferences.forEach {
                        statementBuilder.withReference(it.build(instanceItems))
                    }
                }
            }
        }
    }

    abstract fun tryToFind(iSparql: ISparql, instanceItems: InstanceItems): Publishable

    fun addProperty(remoteProperty: RemoteProperty, value: Value, f: ReferencableValueStatement.() -> Unit = {}) {
        val refStatement = ReferencableValueStatement(remoteProperty, value).apply(f)
        preStatements.add(refStatement)
    }

    fun addProperty(remoteProperty: RemoteProperty, value: String, f: ReferencableValueStatement.() -> Unit = {}) {
        val refStatement = ReferencableValueStatement(remoteProperty, Datamodel.makeStringValue(value)).apply(f)
        preStatements.add(refStatement)
    }
}

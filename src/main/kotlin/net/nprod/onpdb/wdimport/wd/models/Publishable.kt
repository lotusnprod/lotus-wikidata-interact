package net.nprod.onpdb.wdimport.wd.models

import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.newDocument
import net.nprod.onpdb.wdimport.wd.newStatement
import net.nprod.onpdb.wdimport.wd.sparql.ISparql
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql
import net.nprod.onpdb.wdimport.wd.statement
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.*
import kotlin.reflect.KProperty1

class ElementNotPublishedError(msg: String): Exception(msg)

typealias RemoteItem = KProperty1<InstanceItems, ItemIdValue>
typealias RemoteProperty = KProperty1<InstanceItems, PropertyIdValue>


abstract class Publishable {
    private var _id: ItemIdValue? = null

    abstract var name: String
    abstract var type: RemoteItem

    var published: Boolean = false

    val id: ItemIdValue
        get() = _id ?: throw ElementNotPublishedError("This element has not been published yet or failed to get published.")

    val preStatements: MutableList<ReferenceableStatement> = mutableListOf()

    fun published(id: ItemIdValue) {
        _id = id
        published = true
    }

    abstract fun dataStatements(): List<ReferenceableStatement>

    fun document(instanceItems: InstanceItems, subject: ItemIdValue? = null): ItemDocument {
        preStatements.addAll(dataStatements())
        return newDocument(name, subject) {
            statement(subject, instanceItems.instanceOf, type.get(instanceItems))

            // We construct the statements according to this instanceItems value
            preStatements.forEach { refStat ->
                when (refStat) {
                    is ReferenceableValueStatement -> statement(subject, refStat, instanceItems)
                    is ReferenceableRemoteItemStatement -> statement(subject, refStat, instanceItems)
                }
            }
            preStatements.clear()
        }
    }

    fun listOfStatementsForUpdate(instanceItems: InstanceItems): List<Statement> {
        return preStatements.map { referenceableStatement ->
            when (referenceableStatement) {
                is ReferenceableValueStatement -> newStatement(
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
                    ReferenceableValueStatement(
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

    fun addProperty(remoteProperty: RemoteProperty, value: Value, f: ReferenceableValueStatement.() -> Unit ={}) {
        val refStatement = ReferenceableValueStatement(remoteProperty, value).apply(f)
        preStatements.add(refStatement)
    }

    fun addProperty(remoteProperty: RemoteProperty, value: String, f: ReferenceableValueStatement.() -> Unit = {}) {
        val refStatement = ReferenceableValueStatement(remoteProperty, Datamodel.makeStringValue(value)).apply(f)
        preStatements.add(refStatement)
    }
}

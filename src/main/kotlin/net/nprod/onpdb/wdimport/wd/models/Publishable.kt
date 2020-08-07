package net.nprod.onpdb.wdimport.wd.models

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.newDocument
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql
import net.nprod.onpdb.wdimport.wd.statement
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.interfaces.Value
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

    fun document(instanceItems: InstanceItems): ItemDocument {
        require(!this.published) { "Cannot request the document of an already published item."}
        preStatements.addAll(dataStatements())
        return newDocument(name) {
            statement(instanceItems.instanceOf, type.get(instanceItems))

            // We construct the statements according to this instanceItems value
            preStatements.forEach { refStat ->
                when (refStat) {
                    is ReferenceableValueStatement -> statement(refStat, instanceItems)
                    is ReferenceableRemoteItemStatement -> statement(refStat, instanceItems)
                }
            }
            preStatements.clear()
        }
    }

    abstract fun tryToFind(wdSparql: WDSparql, instanceItems: InstanceItems): Publishable

    fun addProperty(remoteProperty: RemoteProperty, value: Value, f: ReferenceableValueStatement.() -> Unit ={}) {
        val refStatement = ReferenceableValueStatement(remoteProperty, value).apply(f)
        preStatements.add(refStatement)
    }

    fun addProperty(remoteProperty: RemoteProperty, value: String, f: ReferenceableValueStatement.() -> Unit = {}) {
        val refStatement = ReferenceableValueStatement(remoteProperty, Datamodel.makeStringValue(value)).apply(f)
        preStatements.add(refStatement)
    }
}

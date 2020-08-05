package wd.models

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import wd.InstanceItems

class ElementNotPublishedError(msg: String): Exception(msg)

abstract class Publishable {
    private var _id: ItemIdValue? = null

    var published: Boolean = false

    val id: ItemIdValue
        get() = _id ?: throw ElementNotPublishedError("This element has not been published yet or failed to get published.")

    abstract fun document(instanceItems: InstanceItems): ItemDocument

    fun published(id: ItemIdValue) {
        _id = id
        published = true
    }
}

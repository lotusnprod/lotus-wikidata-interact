package net.nprod.lotus.wdimport.wd.interfaces

import net.nprod.lotus.wdimport.wd.models.Publishable
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

/**
 * An interface to a RDF style store that allows the publication of **Publishables**
 *
 * The purpose is to have a single interface for any kind of document.
 */
interface Publisher {
    /**
     * A counter for new Documents made by that publisher
     */
    val newDocuments: Int
    /**
     * A counter for updated Documents made by that publisher
     */
    val updatedDocuments: Int

    /**
     * Open a connection
     */
    fun connect()

    /**
     * Close the connection
     */
    fun disconnect()

    /**
     * Create a new Property and returns its ID
     */
    fun newProperty(name: String, description: String): PropertyIdValue

    /**
     * Publish the given entity
     * It HAS to call publishable.document
     */
    fun publish(publishable: Publishable, summary: String): ItemIdValue
}
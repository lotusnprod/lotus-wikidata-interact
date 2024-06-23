/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.query

import kotlinx.serialization.Serializable
import net.nprod.lotus.wdimport.wd.InstanceItems
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue

/**
 * How long to keep the connections open
 */
const val KEEP_ALIVE_TIMEOUT: Long = 10_000

/**
 * How long to wait for connection
 */
const val CONNECT_TIMEOUT: Long = 10_000

/**
 * How many times to retry for connection
 */
const val CONNECT_ATTEMPTS: Int = 5

@Serializable
data class SearchInfoResponse(
    val totalhits: Int,
)

@Serializable
data class SearchResponse(
    val ns: Int,
    val title: String,
    val pageid: Long,
    val size: Int,
    val wordcount: Int,
    val snippet: String,
    val timestamp: String,
)

@Serializable
data class QueryResponse(
    val searchinfo: SearchInfoResponse,
    val search: List<SearchResponse>,
)

@Serializable
data class QueryActionResponse(
    val batchcomplete: String,
    val query: QueryResponse,
) {
    /**
     * Get All the Ids for a given instance, they are sorted by numerical order so you can get the earlier one
     */
    fun getAllIdsForInstance(instanceItems: InstanceItems): List<ItemIdValue> =
        this.query.search
            .map { it.title.trimStart('Q').toInt() to it.title }
            .toMap()
            .toSortedMap()
            .values
            .map {
                ItemIdValueImpl.fromId(it, InstanceItems::wdURI.get(instanceItems)) as ItemIdValue
            }
}

/**
 * The interface for querying WikiData directly
 */
interface IWDKT {
    /**
     * Close the connection
     */
    fun close()

    /**
     * Search for a given doi returns the deserialized response
     *
     * @param doi The DOI to search for
     * @return QueryActionResponse or null if no answer
     */
    fun searchDOI(doi: String): QueryActionResponse? = null

    /**
     * Search for an item with a specific property
     */
    fun searchForPropertyValue(
        property: PropertyIdValue,
        value: String,
    ): QueryActionResponse?
}

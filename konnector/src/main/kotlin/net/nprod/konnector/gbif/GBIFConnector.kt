/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.gbif

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@Serializable
data class Occurence(
    val key: String,
    val decimalLongitude: Double? = null,
    val decimalLatitude: Double? = null,
    val acceptedScientificName: String,
)

@Serializable
data class OccurenceSearchResponse(
    val offset: Int? = null,
    val limit: Int? = null,
    val endOfRecords: Boolean,
    val count: Long? = null,
    val results: List<Occurence>,
)

@Serializable
data class TaxonSearchResponse(
    val usageKey: Int? = null,
    val scientificName: String? = null,
    val canonicalName: String? = null,
    val rank: String? = null,
    val status: String? = null,
    val confidence: Int? = null,
    val matchType: String? = null,
    val kinkdom: String? = null,
    val phylum: String? = null,
    val order: String? = null,
    val family: String? = null,
    val genus: String? = null,
    val species: String? = null,
    val kingdomKey: Int? = null,
    val phylumKey: Int? = null,
    val classKey: Int? = null,
    val orderKey: Int? = null,
    val familyKey: Int? = null,
    val genusKey: Int? = null,
    val speciesKey: Int? = null,
    val synonym: Boolean? = null,
    @SerialName("class") val taxoClass: String? = null,
)

@ExperimentalTime
class GBIFConnector constructor(
    private val api: GBIFAPI,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    fun taxonkeyByName(name: String): TaxonSearchResponse {
        val output =
            api.call(
                api.apiURL + "species/match",
                mutableMapOf("name" to name),
            )

        return json.decodeFromString(
            TaxonSearchResponse.serializer(),
            output,
        )
    }

    fun occurenceOfTaxon(
        q: String? = null,
        taxonKey: String? = null,
        limit: Int = 20,
        offset: Int = 0,
        basisOfRecord: String? = null,
    ): OccurenceSearchResponse {
        val parameters =
            mutableMapOf(
                "limit" to "$limit",
                "offset" to "$offset",
            )

        taxonKey?.let {
            parameters["taxonKey"] = it
        }

        basisOfRecord?.let {
            parameters["basisOfRecord"] = it
        }

        q?.let {
            parameters["q"] = it
        }

        val output =
            api.call(
                api.apiURL + "occurrence/search",
                parameters,
            )

        return json.decodeFromString(
            OccurenceSearchResponse.serializer(),
            output,
        )
    }
}

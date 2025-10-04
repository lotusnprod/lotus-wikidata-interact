/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.globalnames.verify

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@Serializable
data class Version(
    val version: String,
    val build: String,
)

@Serializable
data class DataSource(
    val id: Int,
    val uuid: String? = null,
    val title: String,
    val titleShort: String,
    val description: String? = null,
    val homeURL: String? = null,
    val isOutlinkReady: Boolean? = null,
    val curation: String,
    val recordCount: Long,
    val updatedAt: String,
)

@Serializable
data class VerificationQuery(
    val nameStrings: List<String>,
    val preferredSources: List<Int>,
    val withVernaculars: Boolean,
)

@Serializable
data class Kingdoms(
    val name: String,
    val namesNum: Int,
    val percentage: Double,
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class VerificationMetadata(
    val namesNum: Int? = null,
    val withAllSources: Boolean? = null,
    val withAllMatches: Boolean? = null,
    val withContext: Boolean? = null,
    val withCapitalization: Boolean? = null,
    val dataSources: List<Int>? = null,
    val contextThreshold: Double? = null,
    val contextNamesNum: Int? = null,
    val context: String? = null,
    val contextPercentage: Double? = null,
    val kingdom: String? = null,
    val kingdomPercentage: Double? = null,
    val Kingdoms: List<Kingdoms>? = null,
)

@Serializable
data class ScoreDetails(
    val infraSpecificRankScore: Double,
    val fuzzyLessScore: Double,
    val curatedDataScore: Double,
    val authorMatchScore: Double,
    val acceptedNameScore: Double,
    val parsingQualityScore: Double,
)

@Serializable
data class ResultData(
    val dataSourceId: Int?,
    val dataSourceTitleShort: String,
    val curation: String,
    val recordId: String,
    val globalId: String? = null,
    val localId: String? = null,
    val outlink: String? = null,
    val entryDate: String,
    val sortScore: Double,
    val matchedName: String,
    val matchedCardinality: Int,
    val matchedCanonicalSimple: String? = null,
    val matchedCanonicalFull: String? = null,
    val currentRecordId: String,
    val currentName: String,
    val currentCardinality: Int? = null,
    val currentCanonicalSimple: String? = null,
    val currentCanonicalFull: String? = null,
    val isSynonym: Boolean,
    val classificationPath: String? = null,
    val classificationRanks: String? = null,
    val editDistance: Int,
    val editDistanceStem: Int? = null,
    val matchType: String? = null,
    val scoreDetails: ScoreDetails,
)

@Serializable
data class VerificationName(
    val id: String,
    val name: String,
    val matchType: String,
    val bestResult: ResultData,
    val results: List<ResultData>? = null,
    val dataSourcesNum: Int,
    val curation: String,
    val overloadDetected: String? = null,
    val error: String? = null,
)

@Serializable
data class Verification(
    val metadata: VerificationMetadata,
    val names: List<VerificationName>,
)

/**
 * Connects against GlobalNames verify
 *
 * It is not handling the in query name verification, but that shouldn't change anything for the end user
 *
 * There is some work to do so we get the "possible values" documented in the API doc handled properly:
 * https://app.swaggerhub.com/apis-docs/dimus/gnames/1.0.0#/Verification
 */
@ExperimentalTime
class GlobalNamesVerifyConnector constructor(
    private val api: GlobalNamesVerifyAPI,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    /**
     * Issues a ping request
     *
     * @return true if received pong
     */
    fun ping(): Boolean {
        val output =
            api.callGet(
                api.apiURL + "ping",
            )
        return output == "pong"
    }

    /**
     * Get the version of the endpoint
     */
    fun version(): Version {
        val output =
            api.callGet(
                api.apiURL + "version",
            )
        return json.decodeFromString(
            Version.serializer(),
            output,
        )
    }

    /**
     * Get a list of data sources
     */

    fun dataSources(): List<DataSource> {
        val output =
            api.callGet(
                api.apiURL + "data_sources",
            )
        return json.decodeFromString(
            ListSerializer(DataSource.serializer()),
            output,
        )
    }

    /**
     * Get the info of a specific source
     */

    fun dataSource(id: Int): DataSource {
        if (id <= 0) throw IllegalArgumentException("ID of data source must be greater than 0")
        val output =
            api.callGet(
                api.apiURL + "data_sources/$id",
            )
        return json.decodeFromString(
            DataSource.serializer(),
            output,
        )
    }

    /**
     * Get the info about one or many organisms
     */
    fun verifications(query: VerificationQuery): Verification {
        val output =
            api.callPost(
                api.apiURL + "verifications",
                requestBody = json.encodeToString(query),
            )
        return json.decodeFromString(
            Verification.serializer(),
            output,
        )
    }

/*    fun taxonkeyByName(name: String): TaxonSearchResponse {
        val output = api.call(
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
        basisOfRecord: String? = null
    ): OccurenceSearchResponse {
        val parameters = mutableMapOf(
            "limit" to "$limit",
            "offset" to "$offset"
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

        val output = api.call(
            api.apiURL + "occurrence/search",
            parameters
        )

        return json.decodeFromString(
            OccurenceSearchResponse.serializer(),
            output
        )
    }*/
}

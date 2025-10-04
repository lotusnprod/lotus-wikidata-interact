/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.doi.shortdoi

import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@Serializable
data class ShortDOI(
    @SerialName("DOI") val doi: String,
    @SerialName("ShortDOI") val shortDOI: String,
    @SerialName("IsNew") val isNew: Boolean,
)

@ExperimentalTime
@KtorExperimentalAPI
class ShortDOIConnector constructor(
    private val api: ShortDOIAPI,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    fun shorten(doi: String): ShortDOI {
        val output =
            api.call(
                api.apiURL + doi,
                mutableMapOf("format" to "json"),
            )
        return json.decodeFromString(
            ShortDOI.serializer(),
            output,
        )
    }
}

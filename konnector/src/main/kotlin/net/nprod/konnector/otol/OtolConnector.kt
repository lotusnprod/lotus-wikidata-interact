/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.otol

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@Serializable
data class About(
    val weburl: String,
    val author: String,
    val name: String,
    val source: String,
    val version: String,
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class ExtendedTaxonDescriptor(
    val ott_id: Int,
    val name: String,
    val rank: String,
    val tax_sources: List<String>,
    val unique_name: String,
    val flags: List<String>,
    val synonyms: List<String>,
    val is_suppressed: Boolean,
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class TaxonInfo(
    val ott_id: Int,
    val name: String,
    val rank: String,
    val tax_sources: List<String>,
    val unique_name: String,
    val flags: List<String>,
    val synonyms: List<String>,
    val is_suppressed: Boolean,
    val lineage: List<ExtendedTaxonDescriptor>? = null,
    val children: List<ExtendedTaxonDescriptor>? = null,
    val terminal_descendants: List<Long>? = null,
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class TaxInfoQuery(
    val ott_id: Int? = null,
    val source_id: String? = null,
    val include_children: Boolean,
    val include_lineage: Boolean,
    val include_terminal_descendants: Boolean,
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class MatchNamesQuery(
    val names: List<String>,
    val context_name: String? = null,
    val do_approximate_matching: Boolean = false,
    val include_suppressed: Boolean = false,
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class Matches(
    val is_synonym: Boolean,
    val score: Float,
    val nomenclature_code: String,
    val is_approximate_match: Boolean,
    val taxon: ExtendedTaxonDescriptor,
    val search_string: String,
    val matched_name: String,
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class MatchedNameResult(
    val name: String,
    val matches: List<Matches>,
)

@Serializable
@Suppress("ConstructorParameterNaming")
data class MatchedNames(
    val governing_code: String,
    val unambiguous_names: List<String>,
    val unmatched_names: List<String>,
    val matched_names: List<String>,
    val context: String,
    val includes_deprecated_taxa: Boolean,
    val includes_suppressed_names: Boolean,
    val includes_approximate_matches: Boolean,
    val taxonomy: About,
    val results: List<MatchedNameResult>,
)

/**
 * Connects against OTOL
 *
 * This is currently incomplete, we only have about and taxon_info
 *
 * Following https://github.com/OpenTreeOfLife/germinator/wiki/Taxonomy-API-v3#subtree_taxonomy
 */
@ExperimentalTime
class OtolConnector constructor(
    private val api: OtolAPI,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }

    val taxonomy = Taxonomy()
    val tnrs = Tnrs()

    inner class Taxonomy {
        /**
         * Get the version of the endpoint
         */
        fun about(): About {
            val output =
                api.callPost(
                    api.apiURL + "taxonomy/about",
                )
            return json.decodeFromString(
                About.serializer(),
                output,
            )
        }

        /**
         * Get information on a taxon
         */
        fun taxonInfo(
            ottId: Int? = null,
            sourceId: String? = null,
            includeChildren: Boolean = false,
            includeLineage: Boolean = false,
            includeTerminalDescendants: Boolean = false,
        ): TaxonInfo {
            require((ottId == null && sourceId != null) || (ottId != null && sourceId == null)) {
                "At least ottId or sourceId must be given (and not both)"
            }
            if (sourceId != null) {
                require(listOf("ncbi", "gbif", "worms", "if", "irmng").contains(sourceId.split(":")[0])) {
                    "Source id must be of the form db:id  where db is one of ncbi, gbif, worms, if, irmng"
                }
            }
            val output =
                api.callPost(
                    api.apiURL + "taxonomy/taxon_info",
                    requestBody =
                        json.encodeToString(
                            TaxInfoQuery.serializer(),
                            TaxInfoQuery(
                                ottId,
                                sourceId,
                                includeChildren,
                                includeLineage,
                                includeTerminalDescendants,
                            ),
                        ),
                )
            return json.decodeFromString(
                TaxonInfo.serializer(),
                output,
            )
        }
    }

    inner class Tnrs {
        /**
         * Match names
         */
        fun matchNames(
            names: List<String>,
            contextName: String? = null,
            approximateMatching: Boolean = false,
            includeSuppressed: Boolean = false,
        ): MatchedNames {
            if (approximateMatching) {
                require(names.size <= api.otolMaximumQuerySizeFuzzyNameMatch) {
                    "Only 250 entries can be matched with approximate matching"
                }
            } else {
                require(names.size <= api.otolMaximumQuerySizeExactNameMatch) {
                    "Only 1000 entries can be matched with exact matching"
                }
            }
            val output =
                api.callPost(
                    api.apiURL + "tnrs/match_names",
                    requestBody =
                        json.encodeToString(
                            MatchNamesQuery(
                                names,
                                contextName,
                                approximateMatching,
                                includeSuppressed,
                            ),
                        ),
                )
            return json.decodeFromString(
                MatchedNames.serializer(),
                output,
            )
        }
    }
}

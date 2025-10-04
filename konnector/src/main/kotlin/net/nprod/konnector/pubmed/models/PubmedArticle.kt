/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.pubmed.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Author(
    val lastName: String? = null,
    val foreName: String? = null,
    val initials: String? = null,
    val affiliation: String? = null,
)

@Serializable
data class PubmedArticle(
    val pmid: String,
    val title: String,
    val abstractText: String?,
    val authors: List<String>,
    val journal: String?,
    val year: Int?,
    val volume: String?,
    val issue: String?,
    val pages: String?,
    val doi: String?,
    val issn: String?,
    val meshTerms: List<String>,
    val chemicals: List<String>,
    val grantList: List<String>,
    val publicationType: String?,
    val country: String?,
    val affiliation: String?,
    val language: String?,
    val references: List<String>,
    val citedBy: List<String>,
    val commentsCorrections: List<String>,
    val erratumFor: List<String>,
    val erratumIn: List<String>,
    val retractionIn: List<String>,
    val retractionOf: List<String>,
    val updateIn: List<String>,
    val updateOf: List<String>,
    val expressionOfConcernIn: List<String>,
    val expressionOfConcernFor: List<String>,
    val relatedArticles: List<String>,
) {
    @Suppress("unused")
    fun asString(): String = Json.encodeToString(serializer(), this)
}

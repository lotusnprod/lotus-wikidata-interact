/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.crossref

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DateBlock(
    @SerialName("date-time") val datetime: String? = null,
    @SerialName("date-parts") val dateParts: List<List<Int?>>?,
    val timestamp: Long? = null,
    // Missing date-parts
    // Missing timestamp
)

@Serializable
data class Funder(
    @SerialName("DOI") val doi: String? = null,
    val name: String? = null,
    @SerialName("doi-asserted-by") val doiAssertedBy: String? = null,
    val award: List<String>? = null,
)

@Serializable
data class Domain(
    val domain: List<String>? = null,
    @SerialName("crossmark-restriction") val crossmarkRestriction: Boolean? = null,
)

@Serializable
data class Affiliation(
    val name: String? = null,
)

@Serializable
data class Author(
    @SerialName("ORCID") val orcid: String? = null,
    @SerialName("authenticated-orcid") val authenticatedOrcid: Boolean? = null,
    val given: String? = null,
    val family: String? = null,
    val sequence: String? = null,
    val affiliation: List<Affiliation>?,
)

@Serializable
data class Reference(
    val key: String? = null,
    val author: String? = null,
    val volume: String? = null,
    @SerialName("first-page") val firstPage: String? = null,
    val year: String? = null,
    @SerialName("journal-title") val journalTitle: String? = null,
    val doi: String? = null,
    @SerialName("doi-asserted-by") val doiAssertedBy: String? = null,
)

@Serializable
data class Link(
    @SerialName("URL") val url: String,
    @SerialName("content-type") val contentType: String? = null,
    @SerialName("content-version") val contentVersion: String? = null,
    @SerialName("intended-application") val intendedApplication: String? = null,
)

@Serializable
data class Issue(
    @SerialName("published-print") val publishedPrint: DateBlock? = null,
    val issue: String? = null,
)

@Serializable
data class Relation(
    val cities: List<String>? = null,
)

@Serializable
data class ISSN(
    val value: String? = null,
    val type: String? = null,
)

@Serializable
data class Explanation(
    @SerialName("URL") val url: String? = null,
)

@Serializable
data class Group(
    val name: String? = null,
    val label: String? = null,
)

@Serializable
data class Assertion(
    val value: String? = null,
    val name: String? = null,
    val explanation: Explanation? = null,
    val group: Group? = null,
)

@Serializable
data class SingleWork(
    var position: Int = 0,
    val indexed: DateBlock? = null,
    @SerialName("reference-count") val referenceCount: Int? = null,
    val publisher: String? = null,
    val issue: String? = null,
    @SerialName("funder") val funders: List<Funder>? = null,
    @SerialName("content-domain") val contentDomain: Domain? = null,
    @SerialName("short-container-title") val shortContainerTitle: List<String>? = null,
    val abstract: String? = null,
    @SerialName("DOI") val doi: String,
    val type: String? = null,
    val created: DateBlock? = null,
    val page: String? = null,
    @SerialName("update-policy") val updatePolicy: String? = null,
    val source: String? = null,
    @SerialName("is-referenced-by-count") val isReferencedByCount: Int? = null,
    val title: List<String>? = null,
    val prefix: String? = null,
    val volume: String? = null,
    val author: List<Author>? = null,
    val member: String? = null,
    @SerialName("published-online") val publishedOnline: DateBlock? = null,
    @SerialName("published-print") val publishedPrint: DateBlock? = null,
    val reference: List<Reference>? = null,
    @SerialName("container-title") val containerTitle: List<String>? = null,
    @SerialName("original-title") val originalTitle: List<String>? = null,
    val language: String? = null,
    val link: List<Link>? = null,
    val deposited: DateBlock? = null,
    val score: Double? = null,
    val subtitle: List<String>? = null,
    @SerialName("short-title") val shortTitle: List<String>? = null,
    val issued: DateBlock? = null,
    @SerialName("references-count") val referencesCount: Int? = null,
    @SerialName("journal-issue") val journalIssue: Issue? = null,
    @SerialName("URL") val url: String? = null,
    val relation: Relation? = null,
    @SerialName("ISSN") val issn: List<String>? = null,
    @SerialName("issn-type") val issnType: List<ISSN>? = null,
    val assertion: List<Assertion>? = null,
)

@Serializable
data class WorkResponse(
    val status: String,
    @SerialName("message-type") val messageType: String? = null,
    @SerialName("message-version") val messageVersion: String? = null,
    val message: SingleWork? = null,
)

@Serializable
class Facets

@Serializable
data class WorkList(
    val facets: Facets? = null,
    val items: List<SingleWork>? = null,
)

@Serializable
data class WorksResponse(
    val status: String,
    @SerialName("message-type") val messageType: String? = null,
    @SerialName("message-version") val messageVersion: String? = null,
    val message: WorkList? = null,
)

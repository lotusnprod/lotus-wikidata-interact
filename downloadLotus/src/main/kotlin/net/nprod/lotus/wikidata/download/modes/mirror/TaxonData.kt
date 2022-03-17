/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021
 */

package net.nprod.lotus.wikidata.download.modes.mirror

data class TaxonData(
    val name: String,
    val wikidataId: String,
    val algaeId: String?,
    val birdlifeId: String?,
    val inaturalistId: String?,
    val indexFungorumId: String?,
    val irmngId: String?,
    val itisId: String?,
    val gbifId: String?,
    val mswId: String?,
    val ncbiId: String?,
    val otlId: String?,
    val vascanId: String?,
    val wfoId: String?,
    val wormsId: String?
)

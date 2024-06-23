/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.jobs

data class LotusRawReference(
    val title: String,
    val pmcid: String?,
    val pmid: String?,
    val doi: String,
)

/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.jobs

data class LotusRawOrganism(
    val organismCleaned: String,
    val organismDb: String,
    val organismID: String,
    val organismRanks: String,
    val organismNames: String
)

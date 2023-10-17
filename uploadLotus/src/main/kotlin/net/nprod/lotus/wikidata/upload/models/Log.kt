/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.models

import kotlinx.serialization.Serializable

@Serializable
data class Log(
    val processed: Int,
    val validated: Int,
    val inserted: Int,
    val failed: Int,
    val logFile: List<String>,
)

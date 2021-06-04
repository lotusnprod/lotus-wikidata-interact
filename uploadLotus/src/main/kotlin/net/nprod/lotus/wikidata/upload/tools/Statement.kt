/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.tools

data class Statement(
    val compoundId: String,
    val taxonId: String,
    val referenceId: String
)

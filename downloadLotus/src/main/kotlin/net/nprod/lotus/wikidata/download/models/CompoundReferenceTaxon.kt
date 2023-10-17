/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021
 */

package net.nprod.lotus.wikidata.download.models

data class CompoundReferenceTaxon(
    val compound: String,
    val taxon: String,
    val reference: String?,
)

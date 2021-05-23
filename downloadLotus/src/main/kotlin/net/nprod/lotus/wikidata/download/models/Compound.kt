/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021
 */

package net.nprod.lotus.wikidata.download.models

data class Compound(
    val wikidataId: String,
    val canonicalSmiles: List<String>,
    val isomericSmiles: List<String>,
    val inchis: List<String>,
    val inchiKeys: List<String>
) {
    companion object // We do that to allow other classes to create companion objects.
}

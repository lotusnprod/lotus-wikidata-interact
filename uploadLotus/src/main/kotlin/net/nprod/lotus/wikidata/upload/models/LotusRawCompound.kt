/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.jobs

data class LotusRawCompound(
    val smiles: String,
    val inchi: String,
    val inchiKey: String,
    val compoundName: String,
    val iupacName: String,
    val unspecifiedStereocenters: Int,
    val totalCenters: Int,
)

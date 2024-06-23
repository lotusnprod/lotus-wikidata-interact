/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.taxa

fun fixNothospecies(taxon: String): String =
    taxon
        .replace("× ", "×")

fun fixSpecies(taxon: String): String = fixNothospecies(taxon)

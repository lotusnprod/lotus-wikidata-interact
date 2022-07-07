/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.helpers

fun String.ifEqualReplace(search: String, replaceBy: String): String {
    if (this == search) return replaceBy
    return this
}

fun String.titleCleaner(): String = this.ifEqualReplace("NA", "")
    .replace("\\s+".toRegex(), " ")
    .replace(" ", " ")
    .replace("\n", "")
    .replace("\r", "")

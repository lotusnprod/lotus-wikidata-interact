/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models

/**
 * Represent an author
 *
 * @param ORCID ORCID of that author
 * @param givenName First name
 * @param familyName Family name
 * @property fullName generated from the givenName and the family name
 */
data class AuthorInfo(
    val ORCID: String?,
    val givenName: String,
    val familyName: String
) {
    val fullName: String
        get() = "$givenName $familyName"
}
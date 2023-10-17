/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models

import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue

/**
 * Represent an author
 *
 * @param orcid ORCID of that author
 * @param givenName First name
 * @param familyName Family name (required)
 * @property wikidataID set to the Item ID if this author has been found (for now by its ORCID)
 * @property fullName generated from the givenName and the family name, if no givenName, only family name
 */
data class AuthorInfo(
    val orcid: String?,
    val givenName: String = "",
    val familyName: String,
) {
    var wikidataID: ItemIdValue? = null
    val fullName: String
        get() = if (givenName != "") "$givenName $familyName".trim() else familyName.trim()
}

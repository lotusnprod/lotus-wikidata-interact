/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.otol

import net.nprod.konnector.commons.WebAPI
import kotlin.time.ExperimentalTime

@ExperimentalTime
interface OtolAPI : WebAPI {
    /**
     * location of the API endpoint
     */
    val apiURL: String

    /**
     * Maximum number of queries doable with a fuzzy name match
     */
    val otolMaximumQuerySizeFuzzyNameMatch: Int

    /**
     * Maximum number of queries doable with an exact name match
     */
    val otolMaximumQuerySizeExactNameMatch: Int
}

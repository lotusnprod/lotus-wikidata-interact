/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

@file:Suppress("unused")

package net.nprod.lotus.wdimport.wd.sparql

typealias InChIKey = String

/**
 * Search large quantities of InChIKeys, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findCompoundsByInChIKey(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {},
): Map<InChIKey, List<WDEntity>> = findByPropertyValue("P235", keys, chunkSize, chunkFeedBack)

/**
 * Search large quantities of InChI, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findCompoundsByInChI(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {},
): Map<InChIKey, List<WDEntity>> = findByPropertyValue("P234", keys, chunkSize, chunkFeedBack)

/**
 * Search large quantities of InChIKeys, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findCompoundsByIsomericSMILES(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {},
): Map<InChIKey, List<WDEntity>> = findByPropertyValue("P2017", keys, chunkSize, chunkFeedBack)

/**
 * Search large quantities of InChIKeys, by default they are chunked by groups of 100
 * this can be changed with the `chunkSize` if you have any performance issue
 */
fun WDSparql.findCompoundsByPubChemID(
    keys: List<String>,
    chunkSize: Int = 100,
    chunkFeedBack: () -> Unit = {},
): Map<InChIKey, List<WDEntity>> = findByPropertyValue("P664", keys, chunkSize, chunkFeedBack)

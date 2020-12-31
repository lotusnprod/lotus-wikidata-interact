/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.exceptions

/**
 * Thrown when we have a type that implements ReferencedStatement, but is not handled by all our logic
 */
class UnhandledReferencedStatementType(override val message: String) : RuntimeException()

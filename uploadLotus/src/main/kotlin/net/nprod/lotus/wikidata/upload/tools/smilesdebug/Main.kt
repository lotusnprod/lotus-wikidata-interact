/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.tools.smilesdebug

import org.openscience.cdk.silent.SilentChemObjectBuilder
import org.openscience.cdk.smiles.SmiFlavor
import org.openscience.cdk.smiles.SmilesGenerator
import org.openscience.cdk.smiles.SmilesParser

val canonicalGenerator = SmilesGenerator(SmiFlavor.Canonical)

fun smilesToCanonical(smiles: String): String {
    val atomContainer = SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles)
    return canonicalGenerator.create(atomContainer)
}

fun main() {
    println(
        smilesToCanonical(
            "CC1CCC2(NC1)OC1CC3C4CC=C5CC(OC6OC(CO)C(OC7OC(CO)C(O)C(OC8OCC(O)C(O)C8O)" +
                "C7OC7OC(CO)C(O)C(O)C7O)C(O)C6O)CCC5(C)C4CCC3(C)C1C2C",
        ),
    )
}

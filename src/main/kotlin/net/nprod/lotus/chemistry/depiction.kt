// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.chemistry

import org.openscience.cdk.depict.DepictionGenerator
import org.openscience.cdk.silent.SilentChemObjectBuilder
import org.openscience.cdk.smiles.SmilesParser

fun smilesToSvg(smiles: String): String {
    val atomContainer = SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles)
    val depictionGenerator = DepictionGenerator().withFillToFit().withAtomColors()
    val depiction = depictionGenerator.depict(atomContainer)
    return depiction.toSvgStr()
}

fun main() {
    println(smilesToCanonical("C=C(C)[C@]1(O)CC[C@@]2(C)CCC[C@@](C)(N=C=S)[C@@H]2C1"))
}
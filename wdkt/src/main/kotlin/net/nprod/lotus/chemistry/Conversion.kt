/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.chemistry

import org.openscience.cdk.silent.SilentChemObjectBuilder
import org.openscience.cdk.smiles.SmiFlavor
import org.openscience.cdk.smiles.SmilesGenerator
import org.openscience.cdk.smiles.SmilesParser
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator

fun subscriptFormula(formula: String): String = formula
    .replace("0", "₀")
    .replace("1", "₁")
    .replace("2", "₂")
    .replace("3", "₃")
    .replace("4", "₄")
    .replace("5", "₅")
    .replace("6", "₆")
    .replace("7", "₇")
    .replace("8", "₈")
    .replace("9", "₉")

fun smilesToFormula(smiles: String): String {
    val atomContainer = SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles)
    val molecularFormula = MolecularFormulaManipulator.getMolecularFormula(atomContainer)
    return MolecularFormulaManipulator.getString(molecularFormula)
}

val canonicalGenerator = SmilesGenerator(SmiFlavor.Canonical)

fun smilesToCanonical(smiles: String): String {
    val atomContainer = SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles)
    return canonicalGenerator.create(atomContainer)
}

fun smilesToMass(smiles: String): String {
    val atomContainer = SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles)
    val mass = AtomContainerManipulator.getMass(atomContainer)
    val sb = StringBuilder()
    sb.append("+")
    sb.append(mass)
    sb.append("U483261")
    return sb.toString()
}

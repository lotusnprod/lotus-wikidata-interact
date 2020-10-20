package net.nprod.lotus.chemistry

import org.openscience.cdk.depict.DepictionGenerator
import org.openscience.cdk.silent.SilentChemObjectBuilder
import org.openscience.cdk.smiles.SmilesParser
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator

fun smilesToSvg(smiles: String): String {
    val atomContainer = SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles)
    val depictionGenerator = DepictionGenerator().withFillToFit().withAtomColors()
    val depiction = depictionGenerator.depict(atomContainer)
    return depiction.toSvgStr()
}

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

fun main() {
    println(smilesToSvg("CCC"))
}
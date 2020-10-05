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

fun smilesToFormula(smiles: String): String {
    val atomContainer = SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles)
    val molecularFormula = MolecularFormulaManipulator.getMolecularFormula(atomContainer)
    return MolecularFormulaManipulator.getString(molecularFormula)
}

fun main() {
    println(smilesToSvg("CCC"))
}
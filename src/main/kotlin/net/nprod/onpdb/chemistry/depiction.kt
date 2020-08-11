package net.nprod.onpdb.chemistry

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
    println(smilesToSvg("CCC"))
}
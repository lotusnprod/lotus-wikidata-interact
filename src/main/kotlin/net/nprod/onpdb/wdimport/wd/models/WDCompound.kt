package net.nprod.onpdb.wdimport.wd.models

import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.sparql.ISparql
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue

data class WDCompound(
    override var name: String="",
    val inChIKey: String?,
    val inChI: String?,
    val isomericSMILES: String?,
    val pcId: String? = null,
    val chemicalFormula: String?,
    val f: WDCompound.() -> Unit = {}
) : Publishable() {
    override var type = InstanceItems::chemicalCompound
    private val logger: Logger = LogManager.getLogger(this::class.qualifiedName)

    init {
        apply(f)
    }

    override fun dataStatements() =
        listOfNotNull(
            inChIKey?.let { ReferencableValueStatement(InstanceItems::inChIKey, it) },
            inChI?.let { ReferencableValueStatement(InstanceItems::inChI, it) },
            isomericSMILES?.let { ReferencableValueStatement(InstanceItems::isomericSMILES, it) },
            chemicalFormula?.let { ReferencableValueStatement(InstanceItems::chemicalFormula, it) },
            pcId?.let { ReferencableValueStatement(InstanceItems::pcId, it) }
        )

    override fun tryToFind(iSparql: ISparql, instanceItems: InstanceItems): WDCompound {
        // In the case of the test instance, we do not have the ability to do SPARQL queries

        val query = """
            PREFIX wd: <${InstanceItems::wdURI.get(instanceItems)}>
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            SELECT DISTINCT ?id {
              ?id wdt:${iSparql.resolve(InstanceItems::instanceOf).id} wd:${iSparql.resolve(InstanceItems::chemicalCompound).id};
                  wdt:${iSparql.resolve(InstanceItems::inChIKey).id} ${Rdf.literalOf(inChIKey).queryString}.
            }
            """.trimIndent()

        val results = iSparql.query(query) { result ->
            result.map { bindingSet ->
                bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
            }
        }

        if (results.isNotEmpty()) {
            this.published(
                ItemIdValueImpl.fromId(
                    results.first(),
                    InstanceItems::wdURI.get(instanceItems)
                ) as ItemIdValue
            )
        } else {
            logger.info("This is a new compound!")
        }

        return this
    }


    fun naturalProductOfTaxon(wdTaxon: WDTaxon, f: ReferencableValueStatement.() -> Unit) {
        require(wdTaxon.published) { "Can only add an already published taxon." }
        val refStatement = ReferencableValueStatement(InstanceItems::naturalProductOfTaxon, wdTaxon.id).apply(f)
        preStatements.add(refStatement)
    }

    fun foundInTaxon(wdTaxon: WDTaxon, f: ReferencableValueStatement.() -> Unit) {
        require(wdTaxon.published) { "Can only add an already published taxon." }
        val refStatement = ReferencableValueStatement(InstanceItems::foundInTaxon, wdTaxon.id).apply(f)
        preStatements.add(refStatement)
    }
}
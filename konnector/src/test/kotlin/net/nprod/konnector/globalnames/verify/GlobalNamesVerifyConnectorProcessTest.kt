/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.konnector.globalnames.verify

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Tag("integration")
internal class GlobalNamesVerifyConnectorProcessTest {
    private var connector = GlobalNamesVerifyConnector(OfficialGlobalNamesVerifyAPI())

    @Test
    fun verifications() {
        val source: Verification =
            connector.verifications(
                VerificationQuery(
                    nameStrings =
                        listOf(
                            "Aglaonema simplex",
                            "Arenga caudata",
                            "Disporopsis longifolia",
                            "Dracaena cambodiana",
                            "Dracaena elliptica",
                            "Pollia secundiflora",
                            "Dioscorea cirrhosa",
                            "Rhynchotechum ellipticum",
                            "Gnetum latifolium",
                            "Illigera celebica",
                            "Dichroa febrifuga",
                            "Gonocaryum lobbianum",
                            "Callicarpa macrophylla",
                            "Callicarpa rubella",
                            "Clerodendrum chinense",
                            "Clerodendrum japonicum",
                            "Clerodendrum schmidtii",
                            "Congea tomentosa",
                            "Vitex quinata",
                            "Actinodaphne rehderiana",
                            "Litsea cubeba",
                            "Strychnos nux-blanda",
                            "Hiptage elliptica",
                            "Helicteres viscida",
                            "Microcos paniculata",
                            "Pterospermum argenteum",
                            "Pterospermum insulare",
                            "Pterospermum semisagittatum",
                            "Allomorphia eupteroton",
                            "Medinilla septentrionalis",
                            "Cipadessa baccifera",
                            "Melia azedarach",
                            "Sandoricum koetjape",
                            "Pericampylus glaucus",
                            "Ficus auriculata",
                            "Ficus esquiroliana",
                            "Ficus hispida",
                            "Ficus sarmentosa",
                            "Ficus subincisa",
                            "Knema furfuracea",
                            "Knema globularia",
                            "Gomphia serrata",
                            "Ligustrum robustum",
                            "Ligustrum sinense",
                            "Chaetocarpus castanocarpus",
                            "Antidesma japonicum",
                            "Aporosa octandra",
                            "Baccaurea ramiflora",
                            "Cleistanthus oblongifolius",
                            "Glochidion eriocarpum",
                            "Lophatherum gracile",
                            "Xanthophyllum bibracteatum",
                            "Ardisia annamensis",
                            "Ardisia florida",
                            "Ardisia hanceana",
                            "Ardisia helferiana",
                            "Ardisia humilis",
                            "Ardisia villosa",
                            "Embelia ribes",
                            "Maesa perlaria",
                            "Carallia brachiata",
                            "Pellacalyx yunnanensis",
                            "Rubus alceifolius",
                            "Rubus pluribrateatus",
                            "Chassalia curviflora",
                            "Diplospora dubia",
                            "Gardenia coronaria",
                            "Gardenia stenophylla",
                            "Geophila repens",
                            "Hedyotis vestita",
                            "Ixora chinensis",
                            "Ixora delpyana",
                            "Lasianthus attenuatus",
                            "Lasianthus chinensis",
                            "Mussaenda longipetala",
                            "Oldenlandia microcephala",
                            "Oxyceros horridus",
                            // ...existing code...
                        ),
                    preferredSources = listOf(1, 12, 169),
                    withVernaculars = false,
                ),
            )
        source.names.forEach {
            println("${it.name},${it.bestResult.currentName},${it.bestResult.currentCanonicalSimple}")
        }
    }
}

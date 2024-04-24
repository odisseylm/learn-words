package com.mvv.gnome.gsettings

import com.mvv.gui.util.child
import com.mvv.gui.util.childText
import com.mvv.gui.util.subChilds
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory



private data class InputSourceSimple (
    override val type: String,
    override val id: String,
    override val displayName: String,
    override val shortName: String,
) : InputSource


internal data class InputSourceConfigEntry (
    val name: String,
    val shortDescription: String,
    val description: String,
    val variantList: List<Variant>,

) {
    data class Variant (
        val name: String,
        val description: String,
    )
}



internal fun loadAllXkbInputSources(): List<InputSourceConfigEntry> {

    val dbf = DocumentBuilderFactory.newInstance()
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

    val db = dbf.newDocumentBuilder()
    val doc = db.parse("/usr/share/X11/xkb/rules/base.xml")

    //val xpath: XPath = XPathFactory.newInstance().newXPath();
    //val expr: XPathExpression = xpath.compile("/xkbConfigRegistry/layoutList/layout")
    //val layouts = expr.evaluate(doc, XPathConstants.NODESET) as NodeList

    val layouts = doc.subChilds("xkbConfigRegistry/layoutList/layout")

    val inputSources = layouts.mapNotNull { layout ->
        val configItem = layout.child("configItem") ?: return@mapNotNull null
        val name = configItem.childText("name")
        val shortDescription = configItem.childText("shortDescription")
        val description = configItem.childText("description")

        if (name.isNullOrBlank() || shortDescription.isNullOrBlank() || description.isNullOrBlank())
            return@mapNotNull null

        val variants = layout.subChilds("variantList/variant")
        val variantList = variants
            .map { variant ->
                val varConfigItem = variant.child("configItem") ?: return@mapNotNull null
                InputSourceConfigEntry.Variant(
                    name = varConfigItem.childText("name") ?: "",
                    description = varConfigItem.childText("description") ?: "",
                )
            }
            .filter { it.name.isNotBlank() && it.description.isNotBlank() }

        InputSourceConfigEntry(
            name = name,
            shortDescription = shortDescription,
            description = description,
            variantList = variantList
        )
    }

    return inputSources
}


internal fun InputSourceConfigEntry.toInputSource(): InputSource = InputSourceSimple(
    type  = "xkb",
    id    = name,
    displayName = description,
    shortName   = shortDescription,
)

internal fun InputSourceConfigEntry.toInputSourceWithVariant(variant: InputSourceConfigEntry.Variant): InputSource = InputSourceSimple(
    type  = "xkb",
    id    = name + '+' + variant.name,
    displayName = variant.description,
    shortName   = variant.name,
)


@Suppress("NOTHING_TO_INLINE")
private inline fun CharSequence?.isNotBlank(): Boolean = !this.isNullOrBlank()

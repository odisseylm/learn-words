package com.mvv.gui.memoword

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.FormElement


fun String.parseAsHtml(): Document = Jsoup.parse(this)


fun Document.containsHRef(id: String? = null, innerText: String? = null, ignoreCase: Boolean = false): Boolean {

    val doc = this

    val matched = doc.allElements.asSequence()
        .filter { it.normalName() == "a" }
        .find { el ->
            (id.isNullOrBlank() || id.equals(el.attributeValue("id"), ignoreCase = ignoreCase)) &&
            (innerText.isNullOrBlank() || innerText.equals(el.text(), ignoreCase = ignoreCase))
        }

    return matched != null
}


fun Document.containsInput(id: String? = null, name: String? = null, value: String? = null, ignoreCase: Boolean = false): Boolean {

    val doc = this

    val matched = doc.forms()
        .flatMap { it.elements() }
        .filterIsInstance<FormElement>()
        .find { el ->
            (id.isNullOrBlank()    || id.equals(el.attributeValue("id"), ignoreCase = ignoreCase)) &&
            (name.isNullOrBlank()  || name.equals(el.attributeValue("name"), ignoreCase = ignoreCase)) &&
            (value.isNullOrBlank() || value.equals(el.`val`(), ignoreCase = ignoreCase))
        }

    return matched != null
}

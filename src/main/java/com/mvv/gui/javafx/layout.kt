package com.mvv.gui.javafx

import javafx.geometry.HPos
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Priority



@Suppress("unused")
fun columnConstraints(priority: Priority? = null, hAlignment: HPos? = null, fillWidth: Boolean? = null): ColumnConstraints {

    //val USE_PREF_SIZE = javafx.scene.layout.Region.USE_PREF_SIZE
    //
    //import javafx.scene.layout.Region.USE_PREF_SIZE
    //ColumnConstraints(-1.0, -1.0, -1.0, Priority.NEVER, HPos.RIGHT, false)
    //ColumnConstraints(USE_PREF_SIZE, USE_PREF_SIZE, USE_PREF_SIZE, Priority.NEVER, HPos.RIGHT, false)

    val constr = ColumnConstraints()
    if (priority   != null) constr.hgrow       = priority
    if (hAlignment != null) constr.halignment  = hAlignment
    if (fillWidth  != null) constr.isFillWidth = fillWidth

    return constr
}


fun borderPane(
    center: Node?,
    top: Node? = null,
    left: Node? = null,
    bottom: Node? = null,
    right: Node? = null,
    styleClass: String? = null,
): BorderPane = BorderPane(center).apply {
    if (top    != null) this.top    = top
    if (left   != null) this.left   = left
    if (bottom != null) this.bottom = bottom
    if (right  != null) this.right  = right

    if (styleClass != null) this.styleClass.add(styleClass)
}


fun flowPane(
    children: Iterable<Node>? = null,
    hGap: Double? = null,
    vGap: Double? = null,
    styleClass: String? = null,
): FlowPane = FlowPane().apply {

    if (hGap != null) this.hgap = hGap
    if (vGap != null) this.vgap = vGap

    if (children   != null) this.children.addAll(children)
    if (styleClass != null) this.styleClass.add(styleClass)
}

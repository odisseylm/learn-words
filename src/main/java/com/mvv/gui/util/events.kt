package com.mvv.gui.util

import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import javafx.stage.Window



// TODO: try to remove duplicated code

fun <C: Node, E : Event> C.addOnceEventHandler(eventType: EventType<E>, eventHandler: (E)->Unit) {
    val control = this
    val h: EventHandler<in E> = object : EventHandler<E> {
        override fun handle(event: E) {
            try { eventHandler(event) }
            finally { control.removeEventHandler(eventType, this) }
        }
    }

    control.addEventHandler(eventType, h)
}


fun <W: Window, E : Event> W.addOnceEventHandler(eventType: EventType<E>, eventHandler: (E)->Unit) {
    val window = this
    val h: EventHandler<in E> = object : EventHandler<E> {
        override fun handle(event: E) {
            try { eventHandler(event) }
            finally { window.removeEventHandler(eventType, this) }
        }
    }

    window.addEventHandler(eventType, h)
}

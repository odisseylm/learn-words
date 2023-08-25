package javafx.scene.control.skin


/**
 * Helpers in JavaFX package to have access to internal package level API.
 */

var VirtualFlow<*>.absoluteOffsetValue: Double
    get() = this.absoluteOffset
    set(value) { this.absoluteOffset = value }

fun VirtualFlow<*>.callAdjustPosition() {
    this.adjustPosition()
}

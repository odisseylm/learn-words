package com.mvv.gui.javafx

import javafx.scene.paint.Color
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class JavaFxUtilsTest {

    @Test
    @DisplayName("toCssColor")
    fun test_toCssColor() {
        assertThat(Color.RED.toCssColor()).isEqualTo("#ff0000ff")
    }
}

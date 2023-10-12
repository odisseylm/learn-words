package com.mvv.gui.javafx

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


private val log = mu.KotlinLogging.logger {}


@Disabled("for manual testing") // T O D O: would be nice to rewrite with mock to regular tests
class TestJavaFxPropertyMappings {

    @Test
    fun testObservablePropertyMap() {
        val obj = TempBean()
        log.info { obj.countProperty.value }
        log.info { obj.countProperty.value }
        log.info { obj.countProperty.value }

        obj.baseProperty.set("556")
        log.info { obj.countProperty.value }
        log.info { obj.countProperty.value }
        log.info { obj.countProperty.value }
    }

    @Test
    fun testObservablePropertyMapCached() {
        val obj = TempBean()
        log.info { obj.count2Property.value }
        log.info { obj.count2Property.value }
        log.info { obj.count2Property.value }

        obj.baseProperty.set("557")
        log.info { obj.count2Property.value }
        log.info { obj.count2Property.value }
        log.info { obj.count2Property.value }
    }

    @Test
    fun testObservablePropertyMapCachedAtomic() {
        val obj = TempBean()
        log.info { obj.count3Property.value }
        log.info { obj.count3Property.value }
        log.info { obj.count3Property.value }

        obj.baseProperty.set("558")
        log.info { obj.count3Property.value }
        log.info { obj.count3Property.value }
        log.info { obj.count3Property.value }
    }

    private class TempBean {
        val baseProperty = SimpleStringProperty(this, "base", "11")
        val countProperty:  ObservableValue<Int> = baseProperty.map { log.info { "calculate count" }; it.toInt() }
        val count2Property: ObservableValue<Int> = baseProperty.mapCached { log.info { "calculate count (by cached)" }; it.toInt() }
        val count3Property: ObservableValue<Int> = baseProperty.mapCachedAtomic { log.info { "calculate count (by atomic cache)" }; it.toInt() }
    }
}

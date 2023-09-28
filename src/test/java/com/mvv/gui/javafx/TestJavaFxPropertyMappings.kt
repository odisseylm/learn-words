package com.mvv.gui.javafx

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


@Disabled("for manual testing") // T O D O: would be nice to rewrite with mock to regular tests
class TestJavaFxPropertyMappings {

    @Test
    fun testObservablePropertyMap() {
        val obj = TempBean()
        println(obj.countProperty.value)
        println(obj.countProperty.value)
        println(obj.countProperty.value)

        obj.baseProperty.set("556")
        println(obj.countProperty.value)
        println(obj.countProperty.value)
        println(obj.countProperty.value)
    }

    @Test
    fun testObservablePropertyMapCached() {
        val obj = TempBean()
        println(obj.count2Property.value)
        println(obj.count2Property.value)
        println(obj.count2Property.value)

        obj.baseProperty.set("557")
        println(obj.count2Property.value)
        println(obj.count2Property.value)
        println(obj.count2Property.value)
    }

    @Test
    fun testObservablePropertyMapCachedAtomic() {
        val obj = TempBean()
        println(obj.count3Property.value)
        println(obj.count3Property.value)
        println(obj.count3Property.value)

        obj.baseProperty.set("558")
        println(obj.count3Property.value)
        println(obj.count3Property.value)
        println(obj.count3Property.value)
    }

    private class TempBean {
        val baseProperty = SimpleStringProperty(this, "base", "11")
        val countProperty:  ObservableValue<Int> = baseProperty.map { println("calculate count"); it.toInt() }
        val count2Property: ObservableValue<Int> = baseProperty.mapCached { println("calculate count (by cached)"); it.toInt() }
        val count3Property: ObservableValue<Int> = baseProperty.mapCachedAtomic { println("calculate count (by atomic cache)"); it.toInt() }
    }
}

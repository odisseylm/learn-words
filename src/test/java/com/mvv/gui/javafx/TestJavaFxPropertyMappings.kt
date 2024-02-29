package com.mvv.gui.javafx

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.math.ln
import kotlin.math.pow


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

    /*
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
    */

    private class TempBean {
        val baseProperty = SimpleStringProperty(this, "base", "11")
        val countProperty:  ObservableValue<Int> = baseProperty.map { log.info { "calculate count" }; it.toInt() }
        val count2Property: ObservableValue<Int> = baseProperty.mapCached { log.info { "calculate count (by cached)" }; it.toInt() }
        //val count3Property: ObservableValue<Int> = baseProperty.mapCachedAtomic { log.info { "calculate count (by atomic cache)" }; it.toInt() }
    }

    @Test
    fun aaaa() {
        /*
        println("dsdsd")
        println(pow(sqrt(2.0), sqrt(2.0)))
        println(pow(sqrt(2.0), pow(sqrt(2.0), sqrt(2.0))))
        println(pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), sqrt(2.0)))))
        println(pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), sqrt(2.0))))))
        println(pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), sqrt(2.0)))))))
        println(pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), sqrt(2.0))))))))
        println(pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), pow(sqrt(2.0), sqrt(2.0)))))))))
        */

        println("\n\n")
        for (i in 0..100) {
            val v = i/10.0
            println("$v: " + v.pow(1.0 / v) + "  => " + ffff345(v))
        }

        run {
            val v = Math.E
            println("\n\n$v: " + v.pow(1.0 / v) + "  => " + ffff345(v))
        }
        run {
            val v = Math.E - 0.01
            println("\n\n$v: " + v.pow(1.0 / v) + "  => " + ffff345(v))
        }

        run {
            val v = 2*ln(2.0)/ln(2.0)
            println("\n\n$v: " + v.pow(1.0 / v) + "  => " + ffff345(v))
        }
        run {
            val v = 2*ln(2.1)/ln(2.0)
            println("\n\n$v: " + v.pow(1.0 / v) + "  => " + ffff345(v))
        }

        run {
            val v = 2.0
            println("\n\n$v: " + v.pow(1.0 / v) + "  => " + ffff345(v))
        }
        run {
            val v = 4.0
            println("\n\n$v: " + v.pow(1.0 / v) + "  => " + ffff345(v))
        }

        /*
        println("\n\n")
        //val sqrtN = sqrt(3.0)
        val sqrtN = 2.0.pow(1.0 / 2.0) + 0.005

        //val sqrtN = pow(2.0, 1.0/2.0)
        //val sqrtN = pow(3.0, 1.0/3.0)
        //val sqrtN = pow(4.0, 1.0/4.0)
        //val sqrtN = pow(5.0, 1.0/5.0)
        //val sqrtN = pow(2.0, 1.0/2.0)

        var r = sqrtN
        for (i in 0..1000) {
            r = sqrtN.pow(r)
            println("$i: $r")
        }
        */

        ffff345_2(3.0)
    }
}

private fun ffff345(n: Double): Double {
    val sqrtN = n.pow(1.0 / n)
    var r = sqrtN

    for (i in 0..10_000) {
        r = sqrtN.pow(r)
    }

    return r
}

private fun ffff345_2(n: Double): Double {
    val sqrtN = n.pow(1.0 / n)
    var r = sqrtN

    println("\n\n for n=$n")

    for (i in 0..500) {
        r = sqrtN.pow(r)
        println("iter: $i: $r  (n = $n)")
    }

    return r
}


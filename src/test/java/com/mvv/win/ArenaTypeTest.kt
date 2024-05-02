package com.mvv.win

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import com.mvv.foreign.*
import com.mvv.foreign.MethodHandle
import com.mvv.win.winapi.allocateWinUtf16String
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS


class ArenaTypeTest {

    @Test
    fun useAuto() { useAssertJSoftAssertions {

        val linker = Linker.nativeLinker()
        val strlen: MethodHandle = linker.downcallHandle(
            linker.defaultLookup().find("strlen").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        )

        Arena.ofConfined().use { arena ->
            val str = arena.allocateFrom("Hello") // allocateUtf8String("Hello")
            val len = strlen.invokeExact(str) as Long // 5
            assertThat(len).isEqualTo(5)
        }


        var ml: MemorySegment? = null

        nativeContext(ArenaType.Auto) {
            val str = arena.allocateFrom("Hello")
            ml = str
            val len = strlen.invoke(str) as Long
            assertThat(len).isEqualTo(5)
        }
        assertDoesNotThrow {
            // Actually 'auto' arena can be closed, but it less probable.
            ml?.getAtIndex(ValueLayout.JAVA_BYTE, 0)
        }


        nativeContext(ArenaType.Confined) {
            val str = arena.allocateFrom("Hello")
            ml = str
            val len = strlen.invoke(str) as Long
            assertThat(len).isEqualTo(5)
        }
        assertThatCode { ml?.getAtIndex(ValueLayout.JAVA_BYTE, 0) }
            .message()
            .isIn("Already closed")
    } }


    class DllLoader {
        companion object {
            init {
                println("DllLoader loading")
                //System.load("C:/Windows/System32/msvcrt.dll")
                System.loadLibrary("msvcrt")
            }
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun wcslenTest() { useAssertJSoftAssertions {

        val linker = Linker.nativeLinker()

        Class.forName(DllLoader::class.java.name, true, Arena::class.java.classLoader)
        val wcslen: MethodHandle = linker.downcallHandle(
            linker.defaultLookup().find("wcslen").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        )

        Arena.ofConfined().use { arena ->
            // !? Strange - it adds leading strange 2 bytes.
            //val str = arena.allocateFrom("Hello", Charsets.UTF_16)
            //
            val str = arena.allocateWinUtf16String("Hello")
            val len = wcslen.invokeExact(str) as Long // 5
            assertThat(len).isEqualTo(5)
        }
    } }
}

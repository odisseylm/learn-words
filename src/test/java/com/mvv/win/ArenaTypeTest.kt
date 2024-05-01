package com.mvv.win

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import com.mvv.foreign.*
import java.lang.invoke.MethodHandle


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
}

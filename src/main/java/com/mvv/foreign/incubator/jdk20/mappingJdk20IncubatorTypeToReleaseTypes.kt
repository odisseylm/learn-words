@file:Suppress("PackageDirectoryMismatch", "Since15", "unused")
package com.mvv.foreign

import java.nio.charset.Charset
import java.util.*
import java.lang.foreign.SymbolLookup as JISymbolLookup
import java.lang.foreign.Arena as JIArena


/*
JDK20 feature-preview (incubator) java sample

public class Jdk18IncubatorForeignSample {
    public static void main(String[] args) throws Throwable {
        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlib = linker.defaultLookup();
        MethodHandle strlen = linker.downcallHandle(
            stdlib.find("strlen").get(),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );

        try (Arena arena = Arena.openConfined()) {
            MemorySegment cString = arena.allocateUtf8String("Hello");
            long len = (long)strlen.invoke(cString); // 5
        }
    }
}

See other docs
 https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/lang/foreign/package-summary.html
 https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/lang/foreign/Linker.html

*/


typealias MemoryAddress = Long
typealias MemorySegment = java.lang.foreign.MemorySegment

typealias MemoryLayout  = java.lang.foreign.MemoryLayout
typealias AddressLayout = java.lang.foreign.ValueLayout.OfAddress
typealias ValueLayout   = java.lang.foreign.ValueLayout

typealias OfChar   = java.lang.foreign.ValueLayout.OfChar
typealias OfByte   = java.lang.foreign.ValueLayout.OfByte
typealias OfShort  = java.lang.foreign.ValueLayout.OfShort
typealias OfInt    = java.lang.foreign.ValueLayout.OfInt
typealias OfLong   = java.lang.foreign.ValueLayout.OfLong
typealias OfFloat  = java.lang.foreign.ValueLayout.OfFloat
typealias OfDouble = java.lang.foreign.ValueLayout.OfDouble

typealias GroupLayout  = java.lang.foreign.GroupLayout
typealias StructLayout = java.lang.foreign.StructLayout
typealias PathElement = java.lang.foreign.MemoryLayout.PathElement

typealias MethodHandle = java.lang.invoke.MethodHandle
typealias FunctionDescriptor = java.lang.foreign.FunctionDescriptor
typealias Linker = java.lang.foreign.Linker


class Arena private constructor(
    private val arenaType: ArenaType,
    internal val delegate: JIArena,
    //internal val memorySession: MemorySession,
    ) : AutoCloseable {

    override fun close() {
        if (arenaType.toClose)
            delegate.close()
    }

    // Converts a Java string into a null-terminated C string using the UTF-8 charset
    fun allocateFrom(string: String): MemorySegment = allocateFrom(string, Charsets.UTF_8)

    @Suppress("MemberVisibilityCanBePrivate")
    fun allocateFrom(string: String, charset: Charset): MemorySegment {
        val asBytes = string.toByteArray(charset)

        val bytesEnd = 4L // in the worst case it is utf32 => char size = 4
        val mem = allocate(asBytes.size + bytesEnd)
        for (i in asBytes.indices)
            mem.set(ValueLayout.JAVA_BYTE, i.toLong(), asBytes[i])
        for (i in asBytes.size until asBytes.size + bytesEnd)
            mem.set(ValueLayout.JAVA_BYTE, i, 0)
        return mem
    }

    fun allocate(byteSize: Long): MemorySegment =
        delegate.allocate(byteSize)

    fun allocate(layout: MemoryLayout): MemorySegment =
        delegate.allocate(layout)

    fun allocate(layout: MemoryLayout, count: Long): MemorySegment =
        delegate.allocate(layout.byteSize() * count)

    companion object {
        fun ofConfined(): Arena =
            Arena(ArenaType.Confined, JIArena.openConfined())
        fun ofAuto(): Arena =
            //Arena(ArenaType.Auto, MemorySession.openImplicit())
            throw IllegalStateException("Arena.ofAuto() is not supported in jdk 20")
        fun ofShared(): Arena =
            Arena(ArenaType.Shared, JIArena.openShared())
        fun global(): Arena =
            //Arena(ArenaType.Global, MemorySession.global())
            throw IllegalStateException("Arena.global() is not supported in jdk 20")
    }

    private enum class ArenaType (val toClose: Boolean) {
        Confined(true),
        Auto(false),
        Global(false), // I don't think it should be used in WinAPI code
        Shared(false),
    }
}


class SymbolLookup (internal val delegate: JISymbolLookup) : JISymbolLookup {

    override fun find(name: String): Optional<MemorySegment> = delegate.find(name)

    companion object {
        fun libraryLookup(name: String, arena: Arena): SymbolLookup =
            SymbolLookup(JISymbolLookup.libraryLookup(name, arena.delegate.scope()))
    }
}


@Suppress("UNUSED_PARAMETER")
fun ValueLayout.withTargetLayout(targetLayout: ValueLayout): ValueLayout = this
@Suppress("UNUSED_PARAMETER")
fun ValueLayout.withTargetLayout(targetLayout: GroupLayout): ValueLayout = this
@Suppress("UNUSED_PARAMETER")
fun AddressLayout.withTargetLayout(targetLayout: GroupLayout): AddressLayout = this

fun AddressLayout.withByteAlignment(byteAlignment: Long): AddressLayout = this.withBitAlignment(byteAlignment * 8)
fun ValueLayout.withByteAlignment(byteAlignment: Long): ValueLayout = this.withBitAlignment(byteAlignment * 8)
fun OfChar.withByteAlignment(byteAlignment: Long): OfChar = this.withBitAlignment(byteAlignment * 8)
fun OfByte.withByteAlignment(byteAlignment: Long): OfByte = this.withBitAlignment(byteAlignment * 8)
fun OfShort.withByteAlignment(byteAlignment: Long): OfShort = this.withBitAlignment(byteAlignment * 8)
fun OfInt.withByteAlignment(byteAlignment: Long): OfInt = this.withBitAlignment(byteAlignment * 8)
fun OfLong.withByteAlignment(byteAlignment: Long): OfLong = this.withBitAlignment(byteAlignment * 8)
fun OfFloat.withByteAlignment(byteAlignment: Long): OfFloat = this.withBitAlignment(byteAlignment * 8)
fun OfDouble.withByteAlignment(byteAlignment: Long): OfDouble = this.withBitAlignment(byteAlignment * 8)


fun MemorySegment.getAtIndex(layout: java.lang.foreign.ValueLayout.OfByte, index: Long): Byte =
    this.get(layout, index)
fun MemorySegment.setAtIndex(layout: java.lang.foreign.ValueLayout.OfByte, index: Long, value: Byte) {
    this.set(layout, index, value)
}


// To avoid problems with bit or byte size
fun paddingLayout(layout: MemoryLayout): MemoryLayout = MemoryLayout.paddingLayout(layout.byteSize() * 8)

val NULL_PTR: MemorySegment = MemorySegment.NULL

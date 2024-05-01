@file:Suppress("PackageDirectoryMismatch")
package com.mvv.foreign

//import jdk.incubator.foreign.Addressable
//import jdk.incubator.foreign.FunctionDescriptor
//import jdk.incubator.foreign.MemoryAddress
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.util.Collections
import java.util.Optional


//typealias FunctionDescriptor = java.lang.foreign.FunctionDescriptor
//typealias Linker = java.lang.foreign.Linker
//typealias Linker = jdk.incubator.foreign.CLinker
//typealias FunctionDescriptor = jdk.incubator.foreign.FunctionDescriptor
//typealias ValueLayout = jdk.incubator.foreign.ValueLayout
//typealias MemoryLayout = jdk.incubator.foreign.MemoryLayout
//typealias MemorySegment = jdk.incubator.foreign.MemorySegment
//typealias SymbolLookup = jdk.incubator.foreign.SymbolLookup
//typealias Arena = jdk.incubator.foreign.Arena
//@Suppress("unused")
//fun Linker_nativeLinker(): Linker = Linker.getInstance()


class Linker (
    val impl: jdk.incubator.foreign.CLinker,
    ) {

    fun downcallHandle(address: MemoryAddress, function: FunctionDescriptor): MethodHandle =
        impl.downcallHandle(address.impl, functionDescriptorToMethodType(function), function.impl)

    private fun functionDescriptorToMethodType(function: FunctionDescriptor): MethodType {
        TODO()
    }

    fun defaultLookup(): SymbolLookup = SymbolLookup.defaultLookup()

    companion object {
        @JvmStatic
        fun nativeLinker(): Linker = Linker(jdk.incubator.foreign.CLinker.getInstance())
    }

}


class SymbolLookup (val impl: jdk.incubator.foreign.SymbolLookup) {

    fun find(function: String): Optional<MemoryAddress> =
        impl.lookup(function).map { MemoryAddress(it) }

    companion object {
        private val libs: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())

        @JvmStatic
        fun defaultLookup(): SymbolLookup =
            SymbolLookup(jdk.incubator.foreign.SymbolLookup.loaderLookup())

        @JvmStatic
        fun libraryLookup(libName: String, arena: Arena): SymbolLookup {
            if (libName !in libs) {
                System.loadLibrary(libName)
                libs.add(libName)
            }
            return SymbolLookup(jdk.incubator.foreign.SymbolLookup.loaderLookup())
        }
    }
}

open class FunctionDescriptor (val impl: jdk.incubator.foreign.FunctionDescriptor) {
    companion object {
        @JvmStatic
        fun of(resLayout: MemoryLayout, vararg argLayouts: MemoryLayout): FunctionDescriptor = TODO()
        fun ofVoid(vararg argLayouts: MemoryLayout): FunctionDescriptor = TODO()
    }
}


interface Arena : AutoCloseable {

    fun allocateFrom(string: String): MemorySegment = TODO()
    fun allocate(byteSize: Long): MemorySegment = TODO("Not yet implemented")

    fun allocate(byteSize: MemoryLayout): MemorySegment = TODO("Not yet implemented")
    fun allocate(byteSize: MemoryLayout, count: Long): MemorySegment = TODO("Not yet implemented")

    companion object {
        @JvmStatic
        fun ofConfined(): Arena = TODO()
        @JvmStatic
        fun ofAuto(): Arena = TODO()
    }
}

open class MemoryAddress (val impl: jdk.incubator.foreign.MemoryAddress) {

}

@Suppress("UNUSED_PARAMETER")
open class MemorySegment (val impl: jdk.incubator.foreign.MemorySegment) {
    fun byteSize(): Long = impl.byteSize()

    fun getAtIndex(elementLayout: ValueLayout.OfByte, offset: Long): Byte =
        impl.toByteArray()[offset.toInt()]
    fun getAtIndex(elementLayout: ValueLayout.OfShort, offset: Long): Short =
        impl.toShortArray()[offset.toInt()]
    fun getAtIndex(elementLayout: ValueLayout.OfInt, offset: Long): Int =
        impl.toIntArray()[offset.toInt()]
    fun getAtIndex(elementLayout: ValueLayout.OfLong, offset: Long): Long =
        impl.toLongArray()[offset.toInt()]

    fun setAtIndex(elementLayout: ValueLayout.OfChar, offset: Long, char: Char) {
        impl.asByteBuffer().asCharBuffer().put(offset.toInt(), char)
    }
    fun setAtIndex(elementLayout: ValueLayout.OfByte, offset: Long, byte: Byte) {
        impl.asByteBuffer().put(offset.toInt(), byte)
    }
    fun setAtIndex(elementLayout: ValueLayout.OfShort, offset: Long, short: Short) {
        impl.asByteBuffer().asShortBuffer().put(offset.toInt(), short)
    }
    fun setAtIndex(elementLayout: ValueLayout.OfInt, offset: Long, int: Int) {
        impl.asByteBuffer().asIntBuffer().put(offset.toInt(), int)
    }
    fun setAtIndex(elementLayout: ValueLayout.OfLong, offset: Long, long: Long) {
        impl.asByteBuffer().asLongBuffer().put(offset.toInt(), long)
    }

    fun toArray(elementLayout: ValueLayout.OfChar): CharArray =
        impl.toCharArray()
    fun toArray(elementLayout: ValueLayout.OfByte): ByteArray =
        impl.toByteArray()
    fun toArray(elementLayout: ValueLayout.OfInt): IntArray =
        impl.toIntArray()
    fun toArray(elementLayout: ValueLayout.OfLong): LongArray =
        impl.toLongArray()
}

open class MemoryLayout (val impl: jdk.incubator.foreign.MemoryLayout) {
    fun byteSize(): Long = impl.byteSize()
    open fun withName(name: String): MemoryLayout = MemoryLayout(impl.withName(name))

    companion object {
        @JvmStatic
        fun structLayout(vararg layouts: MemoryLayout): StructLayout = TODO()
    }
}

open class AddressLayout (impl: jdk.incubator.foreign.MemoryLayout) : MemoryLayout(impl) {
    override fun withName(name: String): AddressLayout =
        AddressLayout(impl.withName(name))
    fun withTargetLayout(targetLayout: AddressLayout): AddressLayout = this
    fun withTargetLayout(targetLayout: StructLayout): AddressLayout = this
}

open class ValueLayout (val impl2: jdk.incubator.foreign.ValueLayout) : AddressLayout(impl2) {

    override fun withName(name: String): ValueLayout =
        ValueLayout(impl2.withName(name))

    open class OfChar (impl: jdk.incubator.foreign.ValueLayout) : ValueLayout(impl) {
        override fun withName(name: String): OfChar = OfChar(impl2.withName(name))
    }
    open class OfByte  (impl: jdk.incubator.foreign.ValueLayout) : ValueLayout(impl) {
        override fun withName(name: String): OfByte = OfByte(impl2.withName(name))
    }
    open class OfShort (impl: jdk.incubator.foreign.ValueLayout) : ValueLayout(impl) {
        override fun withName(name: String): OfShort = OfShort(impl2.withName(name))
    }
    open class OfInt (impl: jdk.incubator.foreign.ValueLayout) : ValueLayout(impl) {
        override fun withName(name: String): OfInt = OfInt(impl2.withName(name))
    }
    open class OfLong (impl: jdk.incubator.foreign.ValueLayout) : ValueLayout(impl) {
        override fun withName(name: String): OfLong = OfLong(impl2.withName(name))
    }

    companion object {
        val ADDRESS: ValueLayout = TODO()
        val JAVA_CHAR: OfChar = TODO()
        val JAVA_BYTE: OfByte = TODO()
        val JAVA_SHORT: OfShort = TODO()
        val JAVA_INT: OfInt = TODO()
        val JAVA_LONG: OfLong = TODO()
    }
}

open class StructLayout (impl: jdk.incubator.foreign.MemoryLayout) : MemoryLayout(impl) {
    override fun withName(name: String): StructLayout = StructLayout(impl.withName(name))
}

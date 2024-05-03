@file:Suppress("PackageDirectoryMismatch", "unused")
package com.mvv.foreign//.incubator.jdk18

/*
import com.mvv.gui.util.removeOneOfSuffixes
import java.nio.charset.Charset

import jdk.incubator.foreign.CLinker as JICLinker
import jdk.incubator.foreign.MemoryLayout as JIMemoryLayout
import jdk.incubator.foreign.ValueLayout as JIValueLayout
import jdk.incubator.foreign.ValueLayout as JIMemoryLayouts
import jdk.incubator.foreign.GroupLayout as JIGroupLayout
import jdk.incubator.foreign.FunctionDescriptor as JIFunctionDescriptor
import jdk.incubator.foreign.MemorySegment as JIMemorySegment
import jdk.incubator.foreign.MemoryAddress as JIMemoryAddress
import jdk.incubator.foreign.SymbolLookup as JISymbolLookup
import jdk.incubator.foreign.ResourceScope as JIResourceScope
import jdk.incubator.foreign.MemoryLayout.PathElement as JIPathElement
import jdk.incubator.foreign.NativeSymbol as JINativeSymbol

import java.lang.invoke.MethodHandle as JMethodHandle
import java.lang.invoke.MethodType as JMethodType

import java.util.*


/*

JDK18 incubator java sample

public class Jdk18IncubatorForeignSample {
    public static void main(String[] args) throws Throwable {
        var linker = CLinker.systemCLinker();
        MethodHandle strlen = linker.downcallHandle(
            linker.lookup("strlen").get(),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );

        try (var scope = ResourceScope.newConfinedScope()) {
            var cString = MemorySegment.allocateNative(5 + 1, scope);
            cString.setUtf8String("Hello");
            long len = (long)strlen.invoke(cString); // 5
        }
    }
}

See other docs
 https://docs.oracle.com/en/java/javase/18/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/package-summary.html
 https://docs.oracle.com/en/java/javase/18/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/CLinker.html

*/


class MethodHandle (private val impl: JMethodHandle) {
    fun invoke(vararg args: Any?): Any? =
        impl.invokeWithArguments(fixArgs(args.toList()))
    fun invokeWithArguments(asList: List<Any?>): Any? =
        impl.invokeWithArguments(fixArgs(asList))
    //inline fun <reified R> invokeExact(vararg args: Any?): R =
    @Suppress("UNCHECKED_CAST")
    fun <R> invokeExact(vararg args: Any?): R =
        impl.invokeWithArguments(fixArgs(args.toList())) as R

    // for compatibility with foreign production
    private fun fixArgs(args: List<Any?>): List<Any?> =
        args.map {
            when (it) {
                is MemorySegment   -> it.impl.address()
                is JIMemorySegment -> it.address()
                else -> it
            }
        }

    fun type(): JMethodType = impl.type()
}


class Linker (
    private val impl: JICLinker,
    ) {

    fun downcallHandle(address: MemoryAddress, function: FunctionDescriptor): MethodHandle =
        MethodHandle(impl.downcallHandle(address.symbol, function.impl))

    fun defaultLookup(): SymbolLookup = SymbolLookup.defaultLookup()

    companion object {
        @JvmStatic
        fun nativeLinker(): Linker = Linker(JICLinker.systemCLinker())
    }
}


class SymbolLookup (
    private val impl: JISymbolLookup,
    private val fallback: JISymbolLookup? = null,
    ) {

    fun find(function: String): Optional<MemoryAddress> {
        var res = impl.lookup(function)
        if (fallback != null && res.isEmpty)
            res = fallback.lookup(function)
        return res.map { MemoryAddress(it) }
    }

    companion object {
        private val libs: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())

        @JvmStatic
        fun defaultLookup(): SymbolLookup =
            SymbolLookup(
                impl = JISymbolLookup.loaderLookup(),
                fallback = { JICLinker.systemCLinker().lookup(it) },
            )

        @JvmStatic
        @Suppress("UNUSED_PARAMETER") // 'arena' is needed for compatibility with 'release' foreign
        fun libraryLookup(libName: String, arena: Arena): SymbolLookup {
            if (libName !in libs) {
                System.loadLibrary(libName.removeOneOfSuffixes(".dll", ".so"))
                libs.add(libName)
            }
            return SymbolLookup(JISymbolLookup.loaderLookup())
        }
    }
}


open class FunctionDescriptor (internal val impl: JIFunctionDescriptor) {
    companion object {
        @JvmStatic
        fun of(resLayout: MemoryLayout, vararg argLayouts: MemoryLayout): FunctionDescriptor =
            FunctionDescriptor(JIFunctionDescriptor.of(resLayout.impl, *argLayouts.map { it.impl }.toTypedArray()))
        @JvmStatic
        fun ofVoid(vararg argLayouts: MemoryLayout): FunctionDescriptor =
            FunctionDescriptor(JIFunctionDescriptor.ofVoid(*argLayouts.map { it.impl }.toTypedArray()))
    }
}


@Suppress("MemberVisibilityCanBePrivate")
class Arena (
    private val toClose: Boolean,
    private val resourceScope: JIResourceScope,
    ) : AutoCloseable {

    // Converts a Java string into a null-terminated C string using the UTF-8 charset
    fun allocateFrom(string: String): MemorySegment = allocateFrom(string, Charsets.UTF_8)

    fun allocateFrom(string: String, charset: Charset): MemorySegment {
        val asBytes = string.toByteArray(charset)

        val bytesEnd = 4L // in the worst case it is utf32 => char size = 4
        val mem = allocate(asBytes.size + bytesEnd)
        for (i in asBytes.indices)
            mem.setAtIndex(ValueLayout.JAVA_BYTE, i.toLong(), asBytes[i])
        for (i in asBytes.size until asBytes.size + bytesEnd)
            mem.setAtIndex(ValueLayout.JAVA_BYTE, i, 0)
        return mem
    }

    fun allocate(byteSize: Long): MemorySegment =
        MemorySegment(JIMemorySegment.allocateNative(byteSize, resourceScope))

    fun allocate(layout: MemoryLayout): MemorySegment =
        MemorySegment(JIMemorySegment.allocateNative(layout.impl, resourceScope))

    fun allocate(layout: MemoryLayout, count: Long): MemorySegment =
        MemorySegment(JIMemorySegment.allocateNative(layout.impl.byteSize() * count, resourceScope))

    override fun close() {
        if (toClose)
            resourceScope.close()
    }

    companion object {
        @JvmStatic
        fun ofConfined(): Arena =
            Arena(toClose = true, JIResourceScope.newConfinedScope())
        @JvmStatic
        fun ofAuto(): Arena =
            Arena(toClose = false, JIResourceScope.newImplicitScope())
        //@JvmStatic
        //fun ofShared(): Arena =
        //    Arena(toClose = true, JIResourceScope.newSharedScope())
        @JvmStatic
        fun global(): Arena =
            Arena(toClose = false, JIResourceScope.globalScope())
    }
}

class MemoryAddress (
    private val addressDelegate: JIMemoryAddress?,
    private val symbolDelegate: JINativeSymbol?,
) {
    constructor(address: JIMemoryAddress) : this(address, null)
    constructor(symbol: JINativeSymbol) : this(null, symbol)

    val address: JIMemoryAddress get() = addressDelegate!!
    val symbol: JINativeSymbol get() = symbolDelegate!!
}

// !!! This wrapper ignores byte left/right 'direction' !!!
class MemorySegment (val impl: JIMemorySegment) {
    fun byteSize(): Long = impl.byteSize()

    fun getAtIndex(elementLayout: ValueLayout.OfChar, index: Long): Char =
        impl.getAtIndex(elementLayout.impl3, index)
    fun getAtIndex(elementLayout: ValueLayout.OfByte, index: Long): Byte =
        impl.get(elementLayout.impl3, index)
    fun getAtIndex(elementLayout: ValueLayout.OfShort, index: Long): Short =
        impl.getAtIndex(elementLayout.impl3, index)
    fun getAtIndex(elementLayout: ValueLayout.OfInt, index: Long): Int =
        impl.getAtIndex(elementLayout.impl3, index)
    fun getAtIndex(elementLayout: ValueLayout.OfLong, index: Long): Long =
        impl.getAtIndex(elementLayout.impl3, index)
    fun getAtIndex(elementLayout: ValueLayout.OfFloat, index: Long): Float =
        impl.getAtIndex(elementLayout.impl3, index)
    fun getAtIndex(elementLayout: ValueLayout.OfDouble, index: Long): Double =
        impl.getAtIndex(elementLayout.impl3, index)

    fun get(elementLayout: ValueLayout.OfChar, offset: Long): Char =
        impl.get(elementLayout.impl3, offset)
    fun get(elementLayout: ValueLayout.OfByte, offset: Long): Byte =
        impl.get(elementLayout.impl3, offset)
    fun get(elementLayout: ValueLayout.OfShort, offset: Long): Short =
        impl.get(elementLayout.impl3, offset)
    fun get(elementLayout: ValueLayout.OfInt, offset: Long): Int =
        impl.get(elementLayout.impl3, offset)
    fun get(elementLayout: ValueLayout.OfLong, offset: Long): Long =
        impl.get(elementLayout.impl3, offset)
    fun get(elementLayout: ValueLayout.OfFloat, offset: Long): Float =
        impl.get(elementLayout.impl3, offset)
    fun get(elementLayout: ValueLayout.OfDouble, offset: Long): Double =
        impl.get(elementLayout.impl3, offset)

    fun setAtIndex(elementLayout: ValueLayout.OfChar, index: Long, value: Char) =
        impl.setAtIndex(elementLayout.impl3, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfByte, index: Long, value: Byte) =
        impl.set(elementLayout.impl3, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfShort, index: Long, value: Short) =
        impl.setAtIndex(elementLayout.impl3, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfInt, index: Long, value: Int) =
        impl.setAtIndex(elementLayout.impl3, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfLong, index: Long, value: Long) =
        impl.setAtIndex(elementLayout.impl3, index, value)

    fun set(elementLayout: ValueLayout.OfChar, offset: Long, value: Char) =
        impl.set(elementLayout.impl3, offset, value)
    fun set(elementLayout: ValueLayout.OfByte, offset: Long, value: Byte) =
        impl.set(elementLayout.impl3, offset, value)
    fun set(elementLayout: ValueLayout.OfShort, offset: Long, value: Short) =
        impl.set(elementLayout.impl3, offset, value)
    fun set(elementLayout: ValueLayout.OfInt, offset: Long, value: Int) =
        impl.set(elementLayout.impl3, offset, value)
    fun set(elementLayout: ValueLayout.OfLong, offset: Long, value: Long) =
        impl.set(elementLayout.impl3, offset, value)
    fun set(elementLayout: ValueLayout.OfFloat, offset: Long, value: Float) =
        impl.set(elementLayout.impl3, offset, value)
    fun set(elementLayout: ValueLayout.OfDouble, offset: Long, value: Double) =
        impl.set(elementLayout.impl3, offset, value)
    fun set(elementLayout: AddressLayout, offset: Long, value: MemorySegment) =
        //impl.set(elementLayout.impl as JIValueLayout.OfAddress, offset, value.impl)
        impl.set(elementLayout.impl as JIValueLayout.OfAddress, offset, value.impl)

    fun toArray(elementLayout: ValueLayout.OfChar): CharArray =
        impl.toArray(elementLayout.impl3)
    fun toArray(elementLayout: ValueLayout.OfByte): ByteArray =
        impl.toArray(elementLayout.impl3)
    fun toArray(elementLayout: ValueLayout.OfShort): ShortArray =
        impl.toArray(elementLayout.impl3)
    fun toArray(elementLayout: ValueLayout.OfInt): IntArray =
        impl.toArray(elementLayout.impl3)
    fun toArray(elementLayout: ValueLayout.OfLong): LongArray =
        impl.toArray(elementLayout.impl3)
    fun toArray(elementLayout: ValueLayout.OfFloat): FloatArray =
        impl.toArray(elementLayout.impl3)
    fun toArray(elementLayout: ValueLayout.OfDouble): DoubleArray =
        impl.toArray(elementLayout.impl3)
}

open class MemoryLayout (val impl: JIMemoryLayout) {
    class PathElement (val impl: JIPathElement) {
        companion object {
            fun groupElement(name: String): PathElement =
                PathElement(JIPathElement.groupElement(name))
        }
    }

    fun byteSize(): Long = impl.byteSize()
    fun name(): Optional<String> = impl.name()
    open fun withName(name: String): MemoryLayout = MemoryLayout(impl.withName(name))
    open fun withByteAlignment(byteAlignment: Long): MemoryLayout = MemoryLayout(impl.withBitAlignment(byteAlignment * 8))

    companion object {
        @JvmStatic
        fun structLayout(vararg layouts: MemoryLayout): StructLayout {
            val sl: JIGroupLayout = JIMemoryLayout.structLayout(
                *layouts.map { it.impl }.toTypedArray()
            )
            return StructLayout(sl)
        }

        fun paddingLayout(byteSize: Long): MemoryLayout = MemoryLayout(JIMemoryLayout.paddingLayout(byteSize * 8))
    }
}

open class AddressLayout (impl: JIMemoryLayout) : MemoryLayout(impl) {
    override fun withName(name: String): AddressLayout =
        AddressLayout(impl.withName(name))

    @Suppress("UNUSED_PARAMETER")
    fun withTargetLayout(targetLayout: AddressLayout): AddressLayout = this

    @Suppress("UNUSED_PARAMETER")
    fun withTargetLayout(targetLayout: StructLayout): AddressLayout = this

    override fun withByteAlignment(byteAlignment: Long): AddressLayout =
        AddressLayout(impl.withBitAlignment(byteAlignment * 8))
}


open class ValueLayout (val valueImpl: JIValueLayout) : AddressLayout(valueImpl) {

    override fun withName(name: String): ValueLayout =
        ValueLayout(valueImpl.withName(name))
    override fun withByteAlignment(byteAlignment: Long): ValueLayout =
        ValueLayout(valueImpl.withBitAlignment(byteAlignment * 8))

    class OfChar (val impl3: JIValueLayout.OfChar) : ValueLayout(impl3) {
        override fun withName(name: String): OfChar = OfChar(impl3.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfChar =
            OfChar(impl3.withBitAlignment(byteAlignment * 8))
    }
    class OfByte  (val impl3: JIValueLayout.OfByte) : ValueLayout(impl3) {
        override fun withName(name: String): OfByte = OfByte(impl3.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfByte =
            OfByte(impl3.withBitAlignment(byteAlignment * 8))
    }
    class OfShort (val impl3: JIValueLayout.OfShort) : ValueLayout(impl3) {
        override fun withName(name: String): OfShort = OfShort(impl3.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfShort =
            OfShort(impl3.withBitAlignment(byteAlignment * 8))
    }
    class OfInt (val impl3: JIValueLayout.OfInt) : ValueLayout(impl3) {
        override fun withName(name: String): OfInt = OfInt(impl3.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfInt =
            OfInt(impl3.withBitAlignment(byteAlignment * 8))
    }
    class OfLong (val impl3: JIValueLayout.OfLong) : ValueLayout(impl3) {
        override fun withName(name: String): OfLong = OfLong(impl3.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfLong =
            OfLong(impl3.withBitAlignment(byteAlignment * 8))
    }
    class OfFloat (val impl3: JIValueLayout.OfFloat) : ValueLayout(impl3) {
        override fun withName(name: String): OfFloat = OfFloat(impl3.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfFloat =
            OfFloat(impl3.withBitAlignment(byteAlignment * 8))
    }
    class OfDouble (val impl3: JIValueLayout.OfDouble) : ValueLayout(impl3) {
        override fun withName(name: String): OfDouble = OfDouble(impl3.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfDouble =
            OfDouble(impl3.withBitAlignment(byteAlignment * 8))
    }

    companion object {
        val ADDRESS: ValueLayout  = ValueLayout(JIMemoryLayouts.ADDRESS)
        val JAVA_CHAR:   OfChar   = OfChar   (JIMemoryLayouts.JAVA_CHAR)
        val JAVA_BYTE:   OfByte   = OfByte   (JIMemoryLayouts.JAVA_BYTE)
        val JAVA_SHORT:  OfShort  = OfShort  (JIMemoryLayouts.JAVA_SHORT)
        val JAVA_INT:    OfInt    = OfInt    (JIMemoryLayouts.JAVA_INT)
        val JAVA_LONG:   OfLong   = OfLong   (JIMemoryLayouts.JAVA_LONG)
        val JAVA_FLOAT:  OfFloat  = OfFloat  (JIMemoryLayouts.JAVA_FLOAT)
        val JAVA_DOUBLE: OfDouble = OfDouble (JIMemoryLayouts.JAVA_DOUBLE)
    }
}

class StructLayout (impl: JIMemoryLayout) : MemoryLayout(impl) {
    override fun withName(name: String): StructLayout = StructLayout(impl.withName(name))
    fun byteOffset(vararg path: PathElement): Long = impl.byteOffset(*path.map { it.impl }.toTypedArray())
}
*/

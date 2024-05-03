@file:Suppress("PackageDirectoryMismatch", "unused")
package com.mvv.foreign//.incubator.jdk18

import com.mvv.gui.util.removeOneOfSuffixes
import java.nio.charset.Charset
import java.util.*
import jdk.incubator.foreign.CLinker as JICLinker
import jdk.incubator.foreign.FunctionDescriptor as JIFunctionDescriptor
import jdk.incubator.foreign.GroupLayout as JIGroupLayout
import jdk.incubator.foreign.MemoryAddress as JIMemoryAddress
import jdk.incubator.foreign.MemoryLayout as JIMemoryLayout
import jdk.incubator.foreign.MemoryLayout.PathElement as JIPathElement
import jdk.incubator.foreign.MemorySegment as JIMemorySegment
import jdk.incubator.foreign.NativeSymbol as JINativeSymbol
import jdk.incubator.foreign.ResourceScope as JIResourceScope
import jdk.incubator.foreign.SymbolLookup as JISymbolLookup
import jdk.incubator.foreign.ValueLayout as JIMemoryLayouts
import jdk.incubator.foreign.ValueLayout as JIValueLayout
import java.lang.invoke.MethodHandle as JMethodHandle
import java.lang.invoke.MethodType as JMethodType


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


class MethodHandle (private val delegate: JMethodHandle) {
    fun invoke(vararg args: Any?): Any? =
        delegate.invokeWithArguments(fixArgs(args.toList()))
    fun invokeWithArguments(asList: List<Any?>): Any? =
        delegate.invokeWithArguments(fixArgs(asList))
    //inline fun <reified R> invokeExact(vararg args: Any?): R =
    @Suppress("UNCHECKED_CAST")
    fun <R> invokeExact(vararg args: Any?): R =
        delegate.invokeWithArguments(fixArgs(args.toList())) as R

    // for compatibility with foreign production
    private fun fixArgs(args: List<Any?>): List<Any?> =
        args.map {
            when (it) {
                is MemorySegment   -> it.delegate.address()
                is JIMemorySegment -> it.address()
                else -> it
            }
        }

    fun type(): JMethodType = delegate.type()
}


class Linker (private val delegate: JICLinker) {

    fun downcallHandle(address: MemoryAddress, function: FunctionDescriptor): MethodHandle =
        MethodHandle(delegate.downcallHandle(address.symbol, function.delegate))

    fun defaultLookup(): SymbolLookup = SymbolLookup.defaultLookup()

    companion object {
        fun nativeLinker(): Linker = Linker(JICLinker.systemCLinker())
    }
}


class SymbolLookup (
    private val delegate: JISymbolLookup,
    private val fallback: JISymbolLookup? = null,
    ) {

    fun find(function: String): Optional<MemoryAddress> {
        var res = delegate.lookup(function)
        if (fallback != null && res.isEmpty)
            res = fallback.lookup(function)
        return res.map { MemoryAddress(it) }
    }

    companion object {
        private val libs: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())

        fun defaultLookup(): SymbolLookup =
            SymbolLookup(
                delegate = JISymbolLookup.loaderLookup(),
                fallback = { JICLinker.systemCLinker().lookup(it) },
            )

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


open class FunctionDescriptor (internal val delegate: JIFunctionDescriptor) {
    companion object {
        fun of(resLayout: MemoryLayout, vararg argLayouts: MemoryLayout): FunctionDescriptor =
            FunctionDescriptor(JIFunctionDescriptor.of(resLayout.delegate, *argLayouts.map { it.delegate }.toTypedArray()))
        fun ofVoid(vararg argLayouts: MemoryLayout): FunctionDescriptor =
            FunctionDescriptor(JIFunctionDescriptor.ofVoid(*argLayouts.map { it.delegate }.toTypedArray()))
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
        MemorySegment(JIMemorySegment.allocateNative(layout.delegate, resourceScope))

    fun allocate(layout: MemoryLayout, count: Long): MemorySegment =
        MemorySegment(JIMemorySegment.allocateNative(layout.delegate.byteSize() * count, resourceScope))

    override fun close() {
        if (toClose)
            resourceScope.close()
    }

    companion object {
        fun ofConfined(): Arena =
            Arena(toClose = true, JIResourceScope.newConfinedScope())
        fun ofAuto(): Arena =
            Arena(toClose = false, JIResourceScope.newImplicitScope())
        //fun ofShared(): Arena =
        //    Arena(toClose = true, JIResourceScope.newSharedScope())
        fun global(): Arena =
            Arena(toClose = false, JIResourceScope.globalScope())
    }
}


class MemoryAddress (
    private val addressDelegate: JIMemoryAddress?,
    private val symbolDelegate: JINativeSymbol?,
) {
    constructor(address: JIMemoryAddress) : this(address, null)
    constructor(symbol: JINativeSymbol)   : this(null, symbol)

    val address: JIMemoryAddress get() = addressDelegate!!
    val symbol:  JINativeSymbol  get() = symbolDelegate!!
}


// !!! This wrapper ignores byte left/right 'direction' !!!
class MemorySegment (val delegate: JIMemorySegment) {
    fun byteSize(): Long = delegate.byteSize()

    fun getAtIndex(elementLayout: ValueLayout.OfChar, index: Long): Char =
        delegate.getAtIndex(elementLayout.delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfByte, index: Long): Byte =
        delegate.get(elementLayout.delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfShort, index: Long): Short =
        delegate.getAtIndex(elementLayout.delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfInt, index: Long): Int =
        delegate.getAtIndex(elementLayout.delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfLong, index: Long): Long =
        delegate.getAtIndex(elementLayout.delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfFloat, index: Long): Float =
        delegate.getAtIndex(elementLayout.delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfDouble, index: Long): Double =
        delegate.getAtIndex(elementLayout.delegate, index)

    fun get(elementLayout: ValueLayout.OfChar, offset: Long): Char =
        delegate.get(elementLayout.delegate, offset)
    fun get(elementLayout: ValueLayout.OfByte, offset: Long): Byte =
        delegate.get(elementLayout.delegate, offset)
    fun get(elementLayout: ValueLayout.OfShort, offset: Long): Short =
        delegate.get(elementLayout.delegate, offset)
    fun get(elementLayout: ValueLayout.OfInt, offset: Long): Int =
        delegate.get(elementLayout.delegate, offset)
    fun get(elementLayout: ValueLayout.OfLong, offset: Long): Long =
        delegate.get(elementLayout.delegate, offset)
    fun get(elementLayout: ValueLayout.OfFloat, offset: Long): Float =
        delegate.get(elementLayout.delegate, offset)
    fun get(elementLayout: ValueLayout.OfDouble, offset: Long): Double =
        delegate.get(elementLayout.delegate, offset)

    fun setAtIndex(elementLayout: ValueLayout.OfChar, index: Long, value: Char) =
        delegate.setAtIndex(elementLayout.delegate, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfByte, index: Long, value: Byte) =
        delegate.set(elementLayout.delegate, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfShort, index: Long, value: Short) =
        delegate.setAtIndex(elementLayout.delegate, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfInt, index: Long, value: Int) =
        delegate.setAtIndex(elementLayout.delegate, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfLong, index: Long, value: Long) =
        delegate.setAtIndex(elementLayout.delegate, index, value)

    fun set(elementLayout: ValueLayout.OfChar, offset: Long, value: Char) =
        delegate.set(elementLayout.delegate, offset, value)
    fun set(elementLayout: ValueLayout.OfByte, offset: Long, value: Byte) =
        delegate.set(elementLayout.delegate, offset, value)
    fun set(elementLayout: ValueLayout.OfShort, offset: Long, value: Short) =
        delegate.set(elementLayout.delegate, offset, value)
    fun set(elementLayout: ValueLayout.OfInt, offset: Long, value: Int) =
        delegate.set(elementLayout.delegate, offset, value)
    fun set(elementLayout: ValueLayout.OfLong, offset: Long, value: Long) =
        delegate.set(elementLayout.delegate, offset, value)
    fun set(elementLayout: ValueLayout.OfFloat, offset: Long, value: Float) =
        delegate.set(elementLayout.delegate, offset, value)
    fun set(elementLayout: ValueLayout.OfDouble, offset: Long, value: Double) =
        delegate.set(elementLayout.delegate, offset, value)
    fun set(elementLayout: AddressLayout, offset: Long, value: MemorySegment) =
        delegate.set(elementLayout.delegate, offset, value.delegate)

    fun toArray(elementLayout: ValueLayout.OfChar): CharArray =
        delegate.toArray(elementLayout.delegate)
    fun toArray(elementLayout: ValueLayout.OfByte): ByteArray =
        delegate.toArray(elementLayout.delegate)
    fun toArray(elementLayout: ValueLayout.OfShort): ShortArray =
        delegate.toArray(elementLayout.delegate)
    fun toArray(elementLayout: ValueLayout.OfInt): IntArray =
        delegate.toArray(elementLayout.delegate)
    fun toArray(elementLayout: ValueLayout.OfLong): LongArray =
        delegate.toArray(elementLayout.delegate)
    fun toArray(elementLayout: ValueLayout.OfFloat): FloatArray =
        delegate.toArray(elementLayout.delegate)
    fun toArray(elementLayout: ValueLayout.OfDouble): DoubleArray =
        delegate.toArray(elementLayout.delegate)
}


open class MemoryLayout (private val delegate0: JIMemoryLayout) {
    class PathElement (val delegate: JIPathElement) {
        companion object {
            fun groupElement(name: String): PathElement =
                PathElement(JIPathElement.groupElement(name))
        }
    }

    open val delegate: JIMemoryLayout get() = delegate0
    fun byteSize(): Long = delegate.byteSize()
    fun name(): Optional<String> = delegate.name()
    open fun withName(name: String): MemoryLayout = MemoryLayout(delegate.withName(name))
    open fun withByteAlignment(byteAlignment: Long): MemoryLayout = MemoryLayout(delegate.withBitAlignment(byteAlignment * 8))

    companion object {
        fun structLayout(vararg layouts: MemoryLayout): StructLayout {
            val sl: JIGroupLayout = JIMemoryLayout.structLayout(
                *layouts.map { it.delegate }.toTypedArray()
            )
            return StructLayout(sl)
        }

        fun paddingLayout(byteSize: Long): MemoryLayout = MemoryLayout(JIMemoryLayout.paddingLayout(byteSize * 8))
    }
}


open class ValueLayout (private val delegate0: JIValueLayout) : MemoryLayout(delegate0) {

    override val delegate: JIValueLayout get() = delegate0
    override fun withName(name: String): ValueLayout =
        ValueLayout(delegate.withName(name))
    override fun withByteAlignment(byteAlignment: Long): ValueLayout =
        ValueLayout(delegate.withBitAlignment(byteAlignment * 8))

    class OfChar (private val delegate0: JIValueLayout.OfChar) : ValueLayout(delegate0) {
        override val delegate: JIValueLayout.OfChar get() = delegate0
        override fun withName(name: String): OfChar = OfChar(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfChar =
            OfChar(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfByte  (private val delegate0: JIValueLayout.OfByte) : ValueLayout(delegate0) {
        override val delegate: JIValueLayout.OfByte get() = delegate0
        override fun withName(name: String): OfByte = OfByte(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfByte =
            OfByte(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfShort (private val delegate0: JIValueLayout.OfShort) : ValueLayout(delegate0) {
        override val delegate: JIValueLayout.OfShort get() = delegate0
        override fun withName(name: String): OfShort = OfShort(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfShort =
            OfShort(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfInt (private val delegate0: JIValueLayout.OfInt) : ValueLayout(delegate0) {
        override val delegate: JIValueLayout.OfInt get() = delegate0
        override fun withName(name: String): OfInt = OfInt(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfInt =
            OfInt(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfLong (private val delegate0: JIValueLayout.OfLong) : ValueLayout(delegate0) {
        override val delegate: JIValueLayout.OfLong get() = delegate0
        override fun withName(name: String): OfLong = OfLong(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfLong =
            OfLong(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfFloat (private val delegate0: JIValueLayout.OfFloat) : ValueLayout(delegate0) {
        override val delegate: JIValueLayout.OfFloat get() = delegate0
        override fun withName(name: String): OfFloat = OfFloat(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfFloat =
            OfFloat(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfDouble (private val delegate0: JIValueLayout.OfDouble) : ValueLayout(delegate0) {
        override val delegate: JIValueLayout.OfDouble get() = delegate0
        override fun withName(name: String): OfDouble = OfDouble(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfDouble =
            OfDouble(delegate.withBitAlignment(byteAlignment * 8))
    }

    companion object {
        val ADDRESS: AddressLayout  = AddressLayout(JIMemoryLayouts.ADDRESS)
        val JAVA_CHAR:   OfChar   = OfChar   (JIMemoryLayouts.JAVA_CHAR)
        val JAVA_BYTE:   OfByte   = OfByte   (JIMemoryLayouts.JAVA_BYTE)
        val JAVA_SHORT:  OfShort  = OfShort  (JIMemoryLayouts.JAVA_SHORT)
        val JAVA_INT:    OfInt    = OfInt    (JIMemoryLayouts.JAVA_INT)
        val JAVA_LONG:   OfLong   = OfLong   (JIMemoryLayouts.JAVA_LONG)
        val JAVA_FLOAT:  OfFloat  = OfFloat  (JIMemoryLayouts.JAVA_FLOAT)
        val JAVA_DOUBLE: OfDouble = OfDouble (JIMemoryLayouts.JAVA_DOUBLE)
    }
}


open class AddressLayout (private val delegate0: JIMemoryLayouts.OfAddress) : ValueLayout(delegate0) {
    override val delegate: JIMemoryLayouts.OfAddress get() = delegate0

    override fun withName(name: String): AddressLayout = AddressLayout(delegate.withName(name))

    @Suppress("UNUSED_PARAMETER")
    fun withTargetLayout(targetLayout: ValueLayout): AddressLayout = this

    @Suppress("UNUSED_PARAMETER")
    fun withTargetLayout(targetLayout: AddressLayout): AddressLayout = this

    @Suppress("UNUSED_PARAMETER")
    fun withTargetLayout(targetLayout: StructLayout): AddressLayout = this

    override fun withByteAlignment(byteAlignment: Long): AddressLayout =
        AddressLayout(delegate.withBitAlignment(byteAlignment * 8))
}


class StructLayout (delegate: JIMemoryLayout) : MemoryLayout(delegate) {
    override fun withName(name: String): StructLayout = StructLayout(delegate.withName(name))
    fun byteOffset(vararg path: PathElement): Long = delegate.byteOffset(*path.map { it.delegate }.toTypedArray())
}

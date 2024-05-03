@file:Suppress("PackageDirectoryMismatch", "unused")
package com.mvv.foreign//.incubator.jdk17

import com.mvv.gui.util.containsOneOf
import com.mvv.gui.util.removeOneOfSuffixes
import com.mvv.gui.util.trimToNull
import java.nio.charset.Charset

import jdk.incubator.foreign.CLinker as JICLinker
import jdk.incubator.foreign.MemoryAccess as JIMemoryAccess
import jdk.incubator.foreign.MemoryLayout as JIMemoryLayout
import jdk.incubator.foreign.ValueLayout as JIValueLayout
import jdk.incubator.foreign.MemoryLayouts as JIMemoryLayouts
import jdk.incubator.foreign.GroupLayout as JIGroupLayout
import jdk.incubator.foreign.FunctionDescriptor as JIFunctionDescriptor
import jdk.incubator.foreign.MemorySegment as JIMemorySegment
import jdk.incubator.foreign.MemoryAddress as JIMemoryAddress
import jdk.incubator.foreign.SymbolLookup as JISymbolLookup
import jdk.incubator.foreign.ResourceScope as JIResourceScope
import jdk.incubator.foreign.MemoryLayout.PathElement as JIPathElement

import java.lang.invoke.MethodHandle as JMethodHandle
import java.lang.invoke.MethodType as JMethodType

import java.util.*


/*

JDK17 incubator java sample

public class Jdk17IncubatorForeignSample {
    public static void main(String[] args) throws Throwable {
        MethodHandle strlen = CLinker.getInstance().downcallHandle(
                CLinker.systemLookup().lookup("strlen").get(),
                MethodType.methodType(long.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_LONG_LONG, CLinker.C_POINTER)
        );

        try (var scope = ResourceScope.newConfinedScope()) {
            var cString = CLinker.toCString("Hello", scope);
            long len = (long)strlen.invokeExact(cString.address()); // 5
            //Long len = (Long)strlen.invoke(cString.address()); // 5
            System.out.println("len: " + len);
        }
    }
}

See other docs
 https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/package-summary.html
 https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/CLinker.html

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

    fun downcallHandle(address: MemoryAddress, function: FunctionDescriptor): MethodHandle {

        val returnLayout = if (function.returnLayout != null) toExternalLayout(function.returnLayout).delegate else null
        val layouts = function.argumentLayouts.map { toExternalLayout(it).delegate }

        val nativeFC = if (returnLayout == null) JIFunctionDescriptor.ofVoid(*layouts.toTypedArray())
                       else JIFunctionDescriptor.of(returnLayout, *layouts.toTypedArray())
        val methodHandle = functionDescriptorToMethodType(function)

        return MethodHandle(delegate.downcallHandle(address.delegate, methodHandle, nativeFC))
    }

    private fun functionDescriptorToMethodType(function: FunctionDescriptor): JMethodType {
        val returnType = layoutToType(function.returnLayout)
        val args = function.argumentLayouts.map { layoutToType(it) }
        return JMethodType.methodType(returnType, args)
    }

    fun defaultLookup(): SymbolLookup = SymbolLookup.defaultLookup()
    fun systemLookup(): SymbolLookup = SymbolLookup(JICLinker.systemLookup())

    companion object {
        @JvmStatic
        fun nativeLinker(): Linker = Linker(JICLinker.getInstance())
    }

}


private val cIntTypesMap: Map<Long, JIMemoryLayout> = listOf(
        JICLinker.C_CHAR, JICLinker.C_SHORT, JICLinker.C_INT, JICLinker.C_LONG, JICLinker.C_LONG_LONG)
    .associateBy { it.byteSize() }

private fun findCInt(bytesSize: Long): JIMemoryLayout =
    when (bytesSize) {
        1L -> JICLinker.C_CHAR
        2L -> JICLinker.C_SHORT
        4L -> cIntTypesMap[bytesSize]
        8L -> cIntTypesMap[bytesSize]
        16L -> cIntTypesMap[bytesSize] // most probably not supported
        else -> null
    }.let {
        checkNotNull(it) { "C integer type layout for byte size $bytesSize is not found." }
    }


internal fun toExternalLayout(layout: MemoryLayout): MemoryLayout {
    val l: JIMemoryLayout? = when (layout) {
        ValueLayout.ADDRESS     -> JICLinker.C_POINTER
        is ValueLayout.OfByte   -> JICLinker.C_CHAR
        is ValueLayout.OfChar   -> JICLinker.C_SHORT
        is ValueLayout.OfShort  -> JICLinker.C_SHORT
        is ValueLayout.OfInt    -> findCInt(4)
        is ValueLayout.OfLong   -> findCInt(8)
        is ValueLayout.OfFloat  -> JICLinker.C_FLOAT
        is ValueLayout.OfDouble -> JICLinker.C_DOUBLE
        else -> null
    }

    return MemoryLayout(l ?: layout.delegate)
}


internal fun layoutToType(layout: MemoryLayout?): Class<*> {

    val fullName = layout?.delegate?.name()?.orElse(null)?.lowercase()
    val fullName2 = layout?.delegate?.toString()?.lowercase()
    val typeName2 = fullName?.substringAfter("=", "")?.trimToNull()
    val typeName3 = layout.toString().removeSuffix("]").lowercase().substringAfter("=", "").trimToNull()
    val allTypeNames = listOfNotNull(fullName, fullName2, typeName2, typeName3).distinct()

    val asType: Class<*> = when {
        layout === null -> Void::class.java
        layout === ValueLayout.ADDRESS || layout.delegate === JICLinker.C_POINTER ||allTypeNames.containsOneOf("pointer") ->
            JIMemoryAddress::class.java
        layout is ValueLayout.OfChar || allTypeNames.containsOneOf("java_char", "wchar") ->
            Char::class.java
        layout is ValueLayout.OfByte || allTypeNames.containsOneOf("byte", "java_byte") ->
            Byte::class.java
        layout is ValueLayout.OfShort || allTypeNames.containsOneOf("short", "java_short") ->
            Short::class.java
        layout is ValueLayout.OfInt || allTypeNames.containsOneOf("java_int") ->
            Int::class.java
        layout is ValueLayout.OfLong || allTypeNames.containsOneOf("java_long", "b64") ->
            Long::class.java
        else ->
            throw IllegalArgumentException("Unexpected type layout $layout.")
    }

    return asType
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

        @JvmStatic
        fun defaultLookup(): SymbolLookup =
            SymbolLookup(JISymbolLookup.loaderLookup(), JICLinker.systemLookup())

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


open class FunctionDescriptor (
    //internal val delegate: JIFunctionDescriptor,
    internal val returnLayout: MemoryLayout?,
    internal val argumentLayouts: List<MemoryLayout>) {

    companion object {
        @JvmStatic
        fun of(resLayout: MemoryLayout, vararg argLayouts: MemoryLayout): FunctionDescriptor =
            FunctionDescriptor(
                //JIFunctionDescriptor.of(resLayout.impl, *argLayouts.map { it.impl }.toTypedArray()),
                returnLayout = resLayout,
                argumentLayouts = argLayouts.toList(),
            )
        @JvmStatic
        fun ofVoid(vararg argLayouts: MemoryLayout): FunctionDescriptor =
            FunctionDescriptor(
                //JIFunctionDescriptor.ofVoid(*argLayouts.map { it.impl }.toTypedArray()),
                returnLayout = null,
                argumentLayouts = argLayouts.toList(),
            )
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


class MemoryAddress (internal val delegate: JIMemoryAddress)


// !!! This wrapper ignores byte left/right 'direction' !!!
@Suppress("UNUSED_PARAMETER")
class MemorySegment (val delegate: JIMemorySegment) {
    fun byteSize(): Long = delegate.byteSize()

    fun getAtIndex(elementLayout: ValueLayout.OfChar, index: Long): Char =
        JIMemoryAccess.getCharAtIndex(delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfByte, index: Long): Byte =
        JIMemoryAccess.getByteAtOffset(delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfShort, index: Long): Short =
        JIMemoryAccess.getShortAtIndex(delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfInt, index: Long): Int =
        JIMemoryAccess.getIntAtIndex(delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfLong, index: Long): Long =
        JIMemoryAccess.getLongAtIndex(delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfFloat, index: Long): Float =
        JIMemoryAccess.getFloatAtIndex(delegate, index)
    fun getAtIndex(elementLayout: ValueLayout.OfDouble, index: Long): Double =
        JIMemoryAccess.getDoubleAtIndex(delegate, index)

    fun get(elementLayout: ValueLayout.OfChar, offset: Long): Char =
        JIMemoryAccess.getCharAtOffset(delegate, offset)
    fun get(elementLayout: ValueLayout.OfByte, offset: Long): Byte =
        JIMemoryAccess.getByteAtOffset(delegate, offset)
    fun get(elementLayout: ValueLayout.OfShort, offset: Long): Short =
        JIMemoryAccess.getShortAtOffset(delegate, offset)
    fun get(elementLayout: ValueLayout.OfInt, offset: Long): Int =
        JIMemoryAccess.getIntAtOffset(delegate, offset)
    fun get(elementLayout: ValueLayout.OfLong, offset: Long): Long =
        JIMemoryAccess.getLongAtOffset(delegate, offset)
    fun get(elementLayout: ValueLayout.OfFloat, offset: Long): Float =
        JIMemoryAccess.getFloatAtOffset(delegate, offset)
    fun get(elementLayout: ValueLayout.OfDouble, offset: Long): Double =
        JIMemoryAccess.getDoubleAtOffset(delegate, offset)

    fun setAtIndex(elementLayout: ValueLayout.OfChar, index: Long, value: Char) =
        JIMemoryAccess.setCharAtIndex(delegate, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfByte, index: Long, value: Byte) =
        JIMemoryAccess.setByteAtOffset(delegate, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfShort, index: Long, value: Short) =
        JIMemoryAccess.setShortAtOffset(delegate, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfInt, index: Long, value: Int) =
        JIMemoryAccess.setIntAtOffset(delegate, index, value)
    fun setAtIndex(elementLayout: ValueLayout.OfLong, index: Long, value: Long) =
        JIMemoryAccess.setLongAtOffset(delegate, index, value)

    fun set(attr: ValueLayout.OfChar, offset: Long, value: Char) =
        JIMemoryAccess.setCharAtOffset(delegate, offset, value)
    fun set(attr: ValueLayout.OfByte, offset: Long, value: Byte) =
        JIMemoryAccess.setByteAtOffset(delegate, offset, value)
    fun set(attr: ValueLayout.OfShort, offset: Long, value: Short) =
        JIMemoryAccess.setShortAtOffset(delegate, offset, value)
    fun set(attr: ValueLayout.OfInt, offset: Long, value: Int) =
        JIMemoryAccess.setIntAtOffset(delegate, offset, value)
    fun set(attr: ValueLayout.OfLong, offset: Long, value: Long) =
        JIMemoryAccess.setLongAtOffset(delegate, offset, value)
    fun set(attr: ValueLayout.OfFloat, offset: Long, value: Float) =
        JIMemoryAccess.setFloatAtOffset(delegate, offset, value)
    fun set(attr: ValueLayout.OfDouble, offset: Long, value: Double) =
        JIMemoryAccess.setDoubleAtOffset(delegate, offset, value)
    fun set(attr: AddressLayout, offset: Long, value: MemorySegment) =
        JIMemoryAccess.setAddressAtOffset(delegate, offset, value.delegate.address())

    fun toArray(elementLayout: ValueLayout.OfChar): CharArray =
        delegate.toCharArray()
    fun toArray(elementLayout: ValueLayout.OfByte): ByteArray =
        delegate.toByteArray()
    fun toArray(elementLayout: ValueLayout.OfShort): ShortArray =
        delegate.toShortArray()
    fun toArray(elementLayout: ValueLayout.OfInt): IntArray =
        delegate.toIntArray()
    fun toArray(elementLayout: ValueLayout.OfLong): LongArray =
        delegate.toLongArray()
    fun toArray(elementLayout: ValueLayout.OfFloat): FloatArray =
        delegate.toFloatArray()
    fun toArray(elementLayout: ValueLayout.OfDouble): DoubleArray =
        delegate.toDoubleArray()
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

open class AddressLayout (private val delegate0: JIMemoryLayout) : MemoryLayout(delegate0) {
    override val delegate: JIMemoryLayout get() = delegate0

    override fun withName(name: String): AddressLayout =
        AddressLayout(delegate.withName(name))

    @Suppress("UNUSED_PARAMETER")
    fun withTargetLayout(targetLayout: AddressLayout): AddressLayout = this

    @Suppress("UNUSED_PARAMETER")
    fun withTargetLayout(targetLayout: StructLayout): AddressLayout = this

    override fun withByteAlignment(byteAlignment: Long): AddressLayout =
        AddressLayout(delegate.withBitAlignment(byteAlignment * 8))
}


open class ValueLayout (private val delegate0: JIValueLayout) : AddressLayout(delegate0) {
    override val delegate: JIValueLayout get() = delegate0

    override fun withName(name: String): ValueLayout =
        ValueLayout(delegate.withName(name))
    override fun withByteAlignment(byteAlignment: Long): ValueLayout =
        ValueLayout(delegate.withBitAlignment(byteAlignment * 8))

    class OfChar (delegate: JIValueLayout) : ValueLayout(delegate) {
        override fun withName(name: String): OfChar = OfChar(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfChar =
            OfChar(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfByte  (delegate: JIValueLayout) : ValueLayout(delegate) {
        override fun withName(name: String): OfByte = OfByte(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfByte =
            OfByte(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfShort (delegate: JIValueLayout) : ValueLayout(delegate) {
        override fun withName(name: String): OfShort = OfShort(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfShort =
            OfShort(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfInt (delegate: JIValueLayout) : ValueLayout(delegate) {
        override fun withName(name: String): OfInt = OfInt(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfInt =
            OfInt(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfLong (delegate: JIValueLayout) : ValueLayout(delegate) {
        override fun withName(name: String): OfLong = OfLong(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfLong =
            OfLong(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfFloat (delegate: JIValueLayout) : ValueLayout(delegate) {
        override fun withName(name: String): OfFloat = OfFloat(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfFloat =
            OfFloat(delegate.withBitAlignment(byteAlignment * 8))
    }
    class OfDouble (delegate: JIValueLayout) : ValueLayout(delegate) {
        override fun withName(name: String): OfDouble = OfDouble(delegate.withName(name))
        override fun withByteAlignment(byteAlignment: Long): OfDouble =
            OfDouble(delegate.withBitAlignment(byteAlignment * 8))
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

class StructLayout (delegate: JIMemoryLayout) : MemoryLayout(delegate) {
    override fun withName(name: String): StructLayout = StructLayout(delegate.withName(name))
    fun byteOffset(vararg path: PathElement): Long = delegate.byteOffset(*path.map { it.delegate }.toTypedArray())
}

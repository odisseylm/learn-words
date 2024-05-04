@file:Suppress("Since15")
package com.mvv.win

import com.mvv.gui.util.measureTime
import com.mvv.foreign.*
import java.nio.file.Path


private val log = mu.KotlinLogging.logger {}


enum class ArenaType (val toClose: Boolean) {
    Confined(true),
    Auto(false),
    // Global(false), // I don't think it should be used in WinAPI code
    // Shared(false),
}


private fun createContext(arenaType: ArenaType): NativeContext =
    NativeContext(Arena.ofConfined(), arenaType.toClose)

class NativeContext (
    val arena: Arena,
    private val toClose: Boolean
) : AutoCloseable {
    override fun close() {
        if (toClose) arena.close()
    }
}


fun <R> nativeContext(arenaType: ArenaType, action: NativeContext.()->R): R =
    createContext(arenaType).use(action)
fun <R> nativeContext(action: NativeContext.()->R): R = nativeContext(ArenaType.Confined, action)


class NativeMethodHandle (
    val methodHandle: MethodHandle,
    private val methodName: String,
) {
    override fun toString(): String = "$methodName ${methodHandle.type()}"
}


fun NativeContext.functionHandle(
    dllName: Path,
    functionName: String,
    returnLayout: MemoryLayout? = null,
    vararg argLayouts: MemoryLayout,
): NativeMethodHandle {

    val context = this
    val fd = if (returnLayout != null) FunctionDescriptor.of(returnLayout, *argLayouts)
             else FunctionDescriptor.ofVoid(*argLayouts)

    val fMemoryAddr = SymbolLookup
        // Hm... If we pass 'Path' directly (as java.nio.Path) it is used as absolute path (and DLL is not found).
        .libraryLookup(dllName.toString(), context.arena)
        .find(functionName)
        .orElseThrow { IllegalStateException("Function [$functionName] is not found.") }

    val fHandle = Linker.nativeLinker().downcallHandle(fMemoryAddr, fd)
    return NativeMethodHandle(fHandle, functionName)
}


// it can be used instead of standard MethodHandle.invoke() java to use logging
inline fun <reified R> NativeMethodHandle.invoke(vararg args: Any): R =
    //methodHandle.invokeWithArguments(args.asList()) as R
    invokeWithLog(*args) as R

inline fun <reified R> NativeMethodHandle.call(vararg args: Any): R = this.invoke<R>(*args)


fun NativeMethodHandle.invokeWithLog(vararg args: Any?): Any? =
    measureTime(this.description(), log) {
        methodHandle.invokeWithArguments(args.asList()).also {
            dumpNative(this, it) }
    }

fun dumpNative(method: NativeMethodHandle, value: Any?) {
    log.info { "${method.description()} => ${value.nativeToDebugString()}" }
}

fun NativeMethodHandle.description(): String {
    return this.toString()
}

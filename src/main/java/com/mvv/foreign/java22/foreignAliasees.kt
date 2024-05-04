@file:Suppress("Since15", "PackageDirectoryMismatch", "unused")
package com.mvv.foreign //.java22


typealias MemoryAddress = Long
typealias MemorySegment = java.lang.foreign.MemorySegment

typealias MemoryLayout  = java.lang.foreign.MemoryLayout
typealias AddressLayout = java.lang.foreign.AddressLayout
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
typealias Arena = java.lang.foreign.Arena
typealias SymbolLookup = java.lang.foreign.SymbolLookup


// To avoid problems with bit or byte size
fun paddingLayout(layout: MemoryLayout): MemoryLayout = MemoryLayout.paddingLayout(layout.byteSize())

val NULL_PTR: MemorySegment = MemorySegment.NULL

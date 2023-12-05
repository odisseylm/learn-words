package com.mvv.gui.util

import javafx.application.Platform
import java.util.*
import java.util.Collections.singletonList
import kotlin.Comparator
import kotlin.collections.ArrayList


private val log = mu.KotlinLogging.logger {}


inline fun Boolean.doIfTrue(action: ()->Unit) {
    if (this) action()
}
// just alias
inline fun Boolean.doIfSuccess(action: ()->Unit) = this.doIfTrue(action)


//fun <T> List<T>.addIf(predicate: Boolean, additional: ()->Iterable<T>): List<T> =
//    if (predicate) this + additional() else this


fun timerTask(action: ()->Unit): TimerTask =
    object : TimerTask() {
        override fun run() {
            // we need to catch, otherwise java.util.Timer will stop (strange behaviour)
            try { action() }
            catch (ex: Throwable) { log.error(ex) { "Error in timer: $ex" } }
        }
    }

@Suppress("unused")
fun uiTimerTask(action: ()->Unit): TimerTask = timerTask { Platform.runLater(action) }


inline val Int.isEven: Boolean get() = (this % 2) == 0
inline val Int.isOdd: Boolean get() = (this % 2) == 1


fun <T> T.isOneOf(vararg values: T): Boolean = values.any { it == this }


@Suppress("NOTHING_TO_INLINE")
inline fun <T> Iterable<T>.skipFirst(): List<T> = this.drop(1)

@Suppress("NOTHING_TO_INLINE")
inline fun <T> List<T>.skipFirst(): List<T> =
    when (val count = this.size) {
        1    -> // Kind of optimisation that really works (for PrefixFinder).
                // I think because EmptyList is final and do not use any redirections but just have simple inlinable methods.
                emptyList()
        0    -> throw IllegalArgumentException("List is empty")
        else -> this.subList(1, count)
    }


inline fun doTry(block: ()->Unit): Boolean = try { block(); true } catch (_: Exception) { false }

// It works only if specify type explicitly like ` doTry<Int> { 123 } ` :-(
// T O D O: it should be rewritten in some way...
inline fun <T> doTry(block: ()->T): T? = try { block() } catch (_: Exception) { null }
inline fun <T> doTry(block: ()->T, altValue: T): T = try { block() } catch (_: Exception) { altValue }


fun <E: Enum<E>> enumSetOf(v: E): EnumSet<E> = EnumSet.of(v)
fun <E: Enum<E>> enumSetOf(v1: E, v2: E): EnumSet<E> = EnumSet.of(v1, v2)
fun <E: Enum<E>> enumSetOf(v1: E, v2: E, v3: E): EnumSet<E> = EnumSet.of(v1, v2, v3)

/*
inline fun Int.thenCompare(nextCompare: ()->Int): Int = if (this != 0) this else nextCompare()

inline fun <T, F: Comparable<F>> compare(o1: T, o2: T, field: (T)->F): Int = field(o1).compareTo(field(o2))
inline fun <T, F: Comparable<F>> Int.thenCompare(o1: T, o2: T, field: (T)->F): Int =
    if (this != 0) this else field(o1).compareTo(field(o2))
*/

fun Iterable<String>.toIgnoreCaseSet(): NavigableSet<String> {
    val set = TreeSet(String.CASE_INSENSITIVE_ORDER)
    set.addAll(this)
    return set
}

@Suppress("NOTHING_TO_INLINE")
inline fun areAllNull(v1: Any?): Boolean = (v1 == null)
@Suppress("NOTHING_TO_INLINE")
inline fun areAllNull(v1: Any?, v2: Any?): Boolean =
    (v1 == null) && (v2 == null)
@Suppress("NOTHING_TO_INLINE")
inline fun areAllNull(v1: Any?, v2: Any?, v3: Any?): Boolean =
    (v1 == null) && (v2 == null) && (v3 == null)
@Suppress("NOTHING_TO_INLINE")
inline fun areAllNull(v1: Any?, v2: Any?, v3: Any?, v4: Any?): Boolean =
    (v1 == null) && (v2 == null) && (v3 == null) && (v4 == null)
@Suppress("NOTHING_TO_INLINE")
inline fun areAllNull(v1: Any?, v2: Any?, v3: Any?, v4: Any?, v5: Any?): Boolean =
    (v1 == null) && (v2 == null) && (v3 == null) && (v4 == null) && (v5 == null)


@Suppress("NOTHING_TO_INLINE")
inline fun <T, L: MutableCollection<T>> L.addNotNull(v: T?) { if (v != null) this.add(v) }


@Suppress("NOTHING_TO_INLINE")
inline fun <T> listOfNonNulls(v1: T?): List<T> = if (v1 == null) emptyList() else singletonList(v1)
@Suppress("NOTHING_TO_INLINE")
inline fun <T> listOfNonNulls(v1: T?, v2: T?): List<T> =
    if (v1 != null && v2 != null) listOf(v1, v2)
    else if (v1 != null) singletonList(v1)
    else if (v2 != null) singletonList(v2)
    else emptyList()
@Suppress("NOTHING_TO_INLINE")
inline fun <T> listOfNonNulls(v1: T?, v2: T?, v3: T?): List<T> =
    if (areAllNull(v1, v2, v3)) emptyList()
    else ArrayList<T>(3).also {
        it.addNotNull(v1)
        it.addNotNull(v2)
        it.addNotNull(v3)
    }
@Suppress("NOTHING_TO_INLINE")
inline fun <T> listOfNonNulls(v1: T?, v2: T?, v3: T?, v4: T?): List<T> =
    if (areAllNull(v1, v2, v3, v4)) emptyList()
    else ArrayList<T>(4).also {
        it.addNotNull(v1)
        it.addNotNull(v2)
        it.addNotNull(v3)
        it.addNotNull(v4)
    }
@Suppress("NOTHING_TO_INLINE")
inline fun <T> listOfNonNulls(v1: T?, v2: T?, v3: T?, v4: T?, v5: T?): List<T> =
    if (areAllNull(v1, v2, v3, v4, v5)) emptyList()
    else ArrayList<T>(4).also {
        it.addNotNull(v1)
        it.addNotNull(v2)
        it.addNotNull(v3)
        it.addNotNull(v4)
        it.addNotNull(v5)
    }


@Suppress("NOTHING_TO_INLINE")
inline fun <T, L: List<T>> L.subList(fromIndex: Int): List<T> = this.subList(fromIndex, this.size)
//@Suppress("NOTHING_TO_INLINE")
//inline fun <T, L: MutableList<T>> L.mutableSubList(fromIndex: Int): MutableList<T> = this.subList(fromIndex, this.size)


fun <T> treeSet(comparator: Comparator<T>, vararg items: T): TreeSet<T> {
    val set = TreeSet<T>(comparator)
    set.addAll(items.asIterable())
    return set
}

fun treeStringCaseInsensitiveSet(vararg items: String): TreeSet<String> =
    treeSet(String.CASE_INSENSITIVE_ORDER, *items)


//inline fun <T, R : Comparable<R>> Iterable<T>.minBy(selector: (T) -> R): T = this.minByOrNull(selector)!!

@Suppress("NOTHING_TO_INLINE")
inline fun <E, C: MutableCollection<E>> C.addAll(vararg values: E): C {
    this.addAll(values)
    return this
}
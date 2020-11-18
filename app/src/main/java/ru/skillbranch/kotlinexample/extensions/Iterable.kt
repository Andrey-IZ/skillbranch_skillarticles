package ru.skillbranch.kotlinexample.extensions

fun<T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T> {
    return dropLastWhile{ !predicate(it) }.dropLast(1)
}

/**
 * from dropLastWhile
 */
fun <T> List<T>.dropLastUntil2(predicate: (T) -> Boolean): List<T> {
    if (!isEmpty()) {
        val iterator = listIterator(size)
        while (iterator.hasPrevious()) {
            if (predicate(iterator.previous())) {
                return take(iterator.nextIndex())
            }
        }
    }
    return emptyList()
}
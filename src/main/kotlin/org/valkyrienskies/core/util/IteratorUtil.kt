package org.valkyrienskies.core.util

class ConcatIterator<T>(val iterators: Iterator<Iterable<T>>) : Iterator<T> {
    private var current: Iterator<T> = iterators.next().iterator()

    override fun hasNext(): Boolean = current.hasNext() || (iterators.hasNext() &&
        iterators.next().iterator().let { current = it; hasNext() })

    override fun next(): T = if (current.hasNext()) current.next() else {
        current = iterators.next().iterator(); next()
    }
}

class ConcatMutableIterator<T>(val iterators: Iterator<MutableIterable<T>>) : MutableIterator<T> {
    private var current: MutableIterator<T> = iterators.next().iterator()

    override fun hasNext(): Boolean = current.hasNext() || (iterators.hasNext() &&
        iterators.next().iterator().let { current = it; hasNext() })

    override fun next(): T = if (current.hasNext()) current.next() else {
        current = iterators.next().iterator(); next()
    }

    override fun remove() = current.remove()
}

fun <F, T> Iterator<F>.map(transform: (F) -> T): Iterator<T> = object : Iterator<T> {
    override fun hasNext(): Boolean = this@map.hasNext()

    override fun next(): T = transform(this@map.next())
}

fun <F, T> MutableIterator<F>.mutMap(transform: (F) -> T): MutableIterator<T> = object : MutableIterator<T> {
    override fun hasNext(): Boolean = this@mutMap.hasNext()

    override fun next(): T = transform(this@mutMap.next())

    override fun remove() = this@mutMap.remove()
}

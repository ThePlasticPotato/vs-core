package org.valkyrienskies.core.impl.util.cud

import org.jctools.maps.NonBlockingHashMapLong
import org.valkyrienskies.core.api.util.HasId

class IdUpdateQueue<U : HasId>(
    private val combiner: ((U, U) -> U?)
) : Iterable<U> {

    private val queue = NonBlockingHashMapLong<U>()

    /**
     * Iterates updates in the queue from the reading thread, removing them as they're iterated
     */
    override fun iterator(): Iterator<U> = PollingIterator(queue.values.iterator())

    /**
     * Add an update to the queue from the writing thread
     */
    fun update(newUpdate: U) {
        val id = newUpdate.id

        // Only put the new update if a previous update doesn't exist
        val oldUpdate = queue.putIfAbsent(id, newUpdate)

        if (oldUpdate != null) {
            // the new update has not yet been put into the map, let's create a combined update
            val combinedUpdate = combiner(oldUpdate, newUpdate)

            // this is true if the reading thread has not yet polled
            val success = if (combinedUpdate == null) {
                // if the updates cancel each other out, remove it from the queue
                queue.remove(id) != null
            } else {
                // put the combined update into the queue iff the old update has not been polled yet
                queue.replace(id, oldUpdate, combinedUpdate)
            }

            // if the other thread polled the old update while we were combining, add just the new update into the queue
            if (!success) {
                queue.put(id, newUpdate)
            }
        }
    }
}

/**
 * Wraps an iterator and removes after calling next
 */
private class PollingIterator<T>(private val backing: MutableIterator<T>) : Iterator<T> {
    override fun hasNext(): Boolean = backing.hasNext()
    override fun next(): T = backing.next().also { backing.remove() }
}
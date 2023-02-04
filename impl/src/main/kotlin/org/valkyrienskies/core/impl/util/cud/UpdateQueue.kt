package org.valkyrienskies.core.impl.util.cud

import java.util.concurrent.atomic.AtomicReference

class UpdateQueue<U : Any>(
    private val combiner: UpdateCombiner<U>
) {

    private val ref = AtomicReference<U?>(null)

    /**
     * Removes and returns the next update in queue, if it exists
     */
    fun poll(): U? = ref.getAndSet(null)

    fun update(newUpdate: U) {
        // Only put the new update if a previous update doesn't exist
        val didPutNewUpdate = ref.compareAndSet(null, newUpdate)

        // We didn't successfully put the new update
        if (!didPutNewUpdate) {
            // Get the old update
            val oldUpdate = ref.get()

            // Reading thread polled between compareAndSet and get
            if (oldUpdate == null) {
                // put the new update
                ref.set(newUpdate)
                return
            }

            // the new update has not been put into the ref yet, let's create a combined update
            val combinedUpdate = combiner.combine(oldUpdate, newUpdate)

            // Only put the combined update if the old update hasn't yet been polled
            val didPutCombinedUpdate = ref.compareAndSet(oldUpdate, combinedUpdate)

            // Reading thread polled while we were combining
            if (!didPutCombinedUpdate) {
                // put the new update
                ref.set(newUpdate)
                return
            }
        }
    }

}
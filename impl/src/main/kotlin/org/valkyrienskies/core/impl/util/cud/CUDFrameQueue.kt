package org.valkyrienskies.core.impl.util.cud

import it.unimi.dsi.fastutil.longs.*
import org.valkyrienskies.core.api.util.HasId
import java.util.function.LongConsumer

class CUDFrameQueue<C : HasId, U : HasId>(
    private val combiner: UpdateCombiner<U> = UpdateCombiner.clobber()
) {

    private val queue = UpdateQueue<CUDFrame<C, U>> { oldFrame, newFrame ->
        // merge oldFrame and newFrame
        val created = Long2ObjectOpenHashMap(oldFrame.created)
        val updated = Long2ObjectOpenHashMap(oldFrame.updated)
        val removed = LongOpenHashSet(oldFrame.removed)

        newFrame.removed.forEach(LongConsumer {
            created.remove(it)
            updated.remove(it)
        })

        removed.addAll(newFrame.removed)
        created.putAll(newFrame.created)

        Long2ObjectMaps.fastForEach(newFrame.updated) { (id, update) ->
            val previousUpdate = updated.putIfAbsent(id, update)
            if (previousUpdate != null) {
                updated.put(id, combiner.combine(previousUpdate, update))
            }
        }

        createUnmodifiableFrame(created, updated, removed)
    }

    private val createdThisTick = Long2ObjectOpenHashMap<C>()
    private val updatedThisTick = Long2ObjectOpenHashMap<U>()
    private val removedThisTick = LongOpenHashSet()

    fun create(newObj: C) {
        createdThisTick.put(newObj.id, newObj)
    }

    fun update(update: U) {
        val previousUpdate = updatedThisTick.putIfAbsent(update.id, update)
        if (previousUpdate != null) {
            updatedThisTick.put(update.id, combiner.combine(previousUpdate, update))
        }
    }

    fun delete(id: Long) {
        removedThisTick.add(id)
    }

    fun pushFrame() {
        queue.update(createUnmodifiableFrame(
            Long2ObjectOpenHashMap(createdThisTick),
            Long2ObjectOpenHashMap(updatedThisTick),
            LongOpenHashSet(removedThisTick)
        ))

        createdThisTick.clear()
        updatedThisTick.clear()
        removedThisTick.clear()
    }

    fun poll(): CUDFrame<C, U>? = queue.poll()

    private fun createUnmodifiableFrame(created: Long2ObjectMap<C>, updated: Long2ObjectMap<U>, removed: LongSet) = CUDFrame(
        Long2ObjectMaps.unmodifiable(created),
        Long2ObjectMaps.unmodifiable(updated),
        LongSets.unmodifiable(removed)
    )
}

class CUDFrame<C : HasId, U : HasId>(
    val created: Long2ObjectMap<C>,
    val updated: Long2ObjectMap<U>,
    val removed: LongSet
)


fun interface UpdateCombiner<U> {
    fun combine(first: U, second: U): U?

    companion object {
        @JvmStatic
        fun <U> clobber() = UpdateCombiner<U> { _, u -> u }
    }
}
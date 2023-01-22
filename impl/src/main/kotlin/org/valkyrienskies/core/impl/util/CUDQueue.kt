package org.valkyrienskies.core.impl.util

import org.cliffc.high_scale_lib.NonBlockingHashMapLong
import org.valkyrienskies.core.api.util.HasId

/**
 * The CUD queue allows a writing thread to send create/update/delete operations to a reading thread in a
 * thread-safe manner without the queue growing to an unbounded size if the reading thread doesn't read
 */
class CUDQueue<C : HasId, U : HasId>(
    /**
     * The combiner combines two immutable updates and returns a new one.
     * If the combiner returns null, the update is removed
     *
     * The two updates passed to the combiner always have the same ID and the result should also have the same ID
     *
     * If no combiner is specified, new updates overwrite previous ones
     */
    private val combiner: ((U, U) -> U?)? = null
) : Iterable<Action<C, U>> {

    private val actionQueue = NonBlockingHashMapLong<ActionInternal<C, U>>()

    /**
     * Iterated from the reading thread - calling next REMOVES elements!
     */
    override fun iterator(): Iterator<Action<C, U>> = ActionInternalIterator(actionQueue.values.iterator())

    /**
     * Called from the writing thread
     */
    fun create(newObj: C) {
        queueActionInternal(Action.Create(newObj))
    }

    /**
     * Called from the writing thread
     */
    fun delete(id: Long) {
        queueActionInternal(Action.Delete(id))
    }

    fun update(update: U) {
        queueActionInternal(Action.Update(update))
    }

    /**
     * Called from the writing thread
     */
    private fun queueActionInternal(newUpdate: ActionInternal<C, U>) {
        val id = newUpdate.id

        // Only put the new update if a previous update doesn't exist
        val oldUpdate = actionQueue.putIfAbsent(id, newUpdate)

        if (oldUpdate != null) {
            // the new update has not yet been put into the map, let's create a combined update
            val combinedUpdate = combineInternal(oldUpdate, newUpdate)

            // this is true if the reading thread has not yet polled
            val success = if (combinedUpdate == null) {
                // if the updates cancel each other out, remove it from the queue
                actionQueue.remove(id) != null
            } else {
                // put the combined update into the queue iff the old update has not been polled yet
                actionQueue.replace(id, oldUpdate, combinedUpdate)
            }

            // if the other thread polled the old update while we were combining, add just the new update into the queue
            if (!success) {
                actionQueue.put(id, newUpdate)
            }
        }
    }

    private fun <A> combine(oldUpdate: U, newUpdate: U, constructor: (U) -> A): A? {
        val combiner = combiner
        return if (combiner == null) {
            constructor(oldUpdate)
        } else {
            val combined = combiner.invoke(oldUpdate, newUpdate)
            combined?.let { constructor(it) }
        }
    }

    private fun combineInternal(
        oldAction: ActionInternal<C, U>,
        newAction: ActionInternal<C, U>
    ): ActionInternal<C, U>? {
        return when (oldAction) {
            is Action.Create<C> -> when (newAction) {
                is Action.Create<C>, is CreateThenUpdate -> throw IllegalArgumentException("Cannot create the same object twice!")
                is Action.Update<U> -> CreateThenUpdate(oldAction.create, newAction.update)
                is Action.Delete -> null
            }

            is Action.Update<U> -> when (newAction) {
                is Action.Create<C>, is CreateThenUpdate<C, U> ->
                    throw IllegalArgumentException("Cannot update and then create an object!")

                is Action.Update<U> -> combine(oldAction.update, newAction.update) { Action.Update(it) }
                is Action.Delete -> newAction
            }

            is CreateThenUpdate -> when (newAction) {
                is Action.Create<C>, is CreateThenUpdate ->
                    throw IllegalArgumentException("Cannot create the same object twice!")

                is Action.Update<U> -> combine(oldAction.update, newAction.update) {
                    CreateThenUpdate(oldAction.create, it)
                }
                is Action.Delete -> newAction
            }

            is Action.Delete -> when (newAction) {
                is Action.Update<U> -> throw IllegalArgumentException("Cannot delete then update an object!")
                is Action.Delete -> throw IllegalArgumentException("Cannot delete an object twice!")
                else -> newAction
            }
        }
    }
}

sealed interface Action<out C : HasId, out U : HasId> : ActionInternal<C, U>, HasId {
    data class Create<out C : HasId>(val create: C) : Action<C, Nothing>, HasId by create
    data class Update<out U : HasId>(val update: U) : Action<Nothing, U>, HasId by update
    data class Delete(override val id: Long) : Action<Nothing, Nothing>
}

sealed interface ActionInternal<out C : HasId, out U : HasId> : HasId

private data class CreateThenUpdate<out C : HasId, out U : HasId>(
    val create: C,
    val update: U
) : ActionInternal<C, U>, HasId by create {
    init {
        require(create.id == update.id)
    }
}


/**
 * Removes elements whenever calling next(), and converts ActionInternal to Action
 */
private class ActionInternalIterator<out C : HasId, out U : HasId>(
    private val backing: MutableIterator<ActionInternal<C, U>>
) : Iterator<Action<C, U>> {

    private var nextUpdate: U? = null

    override fun hasNext(): Boolean {
        return nextUpdate != null || backing.hasNext()
    }

    override fun next(): Action<C, U> {
        // return nextUpdate if we have one
        nextUpdate?.let {
            nextUpdate = null
            return Action.Update(it)
        }

        val next = backing.next()
        backing.remove()

        // Convert CreateThenUpdate to two separate updates if we have one
        if (next is CreateThenUpdate<C, U>) {
            this.nextUpdate = next.update
            return Action.Create(next.create)
        }

        // Otherwise just return
        return next as Action<C, U>
    }
}

package org.valkyrienskies.core.impl.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.valkyrienskies.core.api.util.HasId

class CUDQueueTest : StringSpec({

    data class UpdateMessage(override val id: Long, val delta: Int) : HasId
    data class CreateMessage(override val id: Long, val value: Int) : HasId

    fun makeQueue() =
        CUDQueue<CreateMessage, UpdateMessage> { u1, u2 -> UpdateMessage(u1.id, u1.delta + u2.delta) }

    "send and receive create messages" {
        val queue = makeQueue()
        queue.create(CreateMessage(0L, 420))

        val iter = queue.iterator()

        val action = iter.next()

        action.shouldBeInstanceOf<Action.Create<CreateMessage>>()

        action.create.id shouldBe 0
        action.create.value shouldBe 420

        queue.iterator().hasNext() shouldBe false
    }

    "merge update messages" {
        val queue = makeQueue()
        queue.create(CreateMessage(0L, 420))
        queue.update(UpdateMessage(0L, 100))
        queue.update(UpdateMessage(0L, -40))

        val iter = queue.iterator()

        val a1 = iter.next()
        a1.shouldBeInstanceOf<Action.Create<CreateMessage>>()
        a1.create.value shouldBe 420

        val a2 = iter.next()
        a2.shouldBeInstanceOf<Action.Update<UpdateMessage>>()
        a2.update.delta shouldBe 60

        queue.iterator().hasNext() shouldBe false
    }


})
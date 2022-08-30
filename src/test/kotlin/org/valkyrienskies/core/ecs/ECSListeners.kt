package org.valkyrienskies.core.ecs

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestComponent : Component

class ECSListeners : StringSpec({

    "add listener" {
        val world = VSWorld()
        var fired = 0

        TestComponent::class.listenOnAdd(world) { entity, testComponent -> fired++ }

        world.spawn("test1", TestComponent())
        world.spawn("test2")
        world.spawn("test3") += TestComponent()

        fired shouldBe 2
    }

    "remove listener" {
        val world = VSWorld()
        var fired = 0

        TestComponent::class.listenOnRemove(world) { entity, testComponent -> fired++ }

        world.spawn("test1", TestComponent())
        val entity2 = world.spawn("test2", TestComponent())
        val entity = world.spawn("test3") + TestComponent()

        entity -= TestComponent::class
        world.delete(entity2)

        fired shouldBe 2
    }
})

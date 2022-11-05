package org.valkyrienskies.core.util.events

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe

class EventEmitterImplTest : AnnotationSpec() {

    @Test
    fun testEventListen() {
        val emitter = EventEmitterImpl<String>()
        var called = 0
        emitter.on { value, _ ->
            called++
            value shouldBe "hi"
        }

        emitter.emit("hi")

        called shouldBeExactly 1
    }

    @Test
    fun testEventListenOnce() {
        val emitter = EventEmitterImpl<String>()

        var called = 0

        emitter.once({ it == "hi" }) { value ->
            called++
            value shouldBe "hi"
        }

        emitter.emit("bye")
        emitter.emit("hi")
        emitter.emit("bye")
        emitter.emit("hi")

        called shouldBeExactly 1
    }

    @Test
    fun testEventListenUnregister() {
        val emitter = EventEmitterImpl<String>()

        var called = 0

        emitter.on { value, handler ->
            called++
            if (value == "unregister") {
                handler.unregister()
            }
        }

        emitter.emit("1")
        emitter.emit("2")
        emitter.emit("3")
        emitter.emit("unregister")
        emitter.emit("4")
        emitter.emit("5")

        called shouldBeExactly 4
    }
}

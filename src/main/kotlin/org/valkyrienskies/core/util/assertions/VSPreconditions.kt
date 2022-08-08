package org.valkyrienskies.core.util.assertions

fun assertIsPhysicsThread() {
    assert(Thread.currentThread().name.startsWith("Physics thread")) { "Not called from physics thread" }
}

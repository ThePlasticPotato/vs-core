package org.valkyrienskies.core.impl.util.assertions

import org.joml.Matrix3dc
import org.joml.Vector3dc


fun requireIsFinite(v: Vector3dc) {
    require(v.isFinite) { "$v is not finite!" }
}

fun assertIsPhysicsThread() {
    assert(Thread.currentThread().name.startsWith("Physics thread")) { "Not called from physics thread" }
}

fun assertIsGameThread() {}
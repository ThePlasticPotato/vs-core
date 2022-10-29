package org.valkyrienskies.core.util

import org.joml.Vector3dc

fun requireIsFinite(v: Vector3dc) {
    require(v.isFinite) { "$v is not finite!" }
}

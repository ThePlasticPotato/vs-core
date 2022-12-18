package org.valkyrienskies.core.util.prelude

import org.joml.Vector3dc
import org.joml.Vector3ic

// Vector3ic
operator fun Vector3ic.component1() = x
operator fun Vector3ic.component2() = y
operator fun Vector3ic.component3() = z

val Vector3ic.x get() = x()
val Vector3ic.y get() = y()
val Vector3ic.z get() = z()

// Vector3dc

operator fun Vector3dc.component1() = x
operator fun Vector3dc.component2() = y
operator fun Vector3dc.component3() = z

val Vector3dc.x get() = x()
val Vector3dc.y get() = y()
val Vector3dc.z get() = z()

// endregion

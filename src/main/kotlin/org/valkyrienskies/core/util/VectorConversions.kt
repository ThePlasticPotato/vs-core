package org.valkyrienskies.core.util

import org.joml.Vector3dc
import org.joml.Vector3ic

// region JOML

// Vector3ic
operator fun Vector3ic.component1() = x
operator fun Vector3ic.component2() = y
operator fun Vector3ic.component3() = z

val Vector3ic.x get() = x()
val Vector3ic.y get() = y()
val Vector3ic.z get() = z()

fun Vector3ic.multiplyTerms() = x * y * z

// Vector3dc

operator fun Vector3dc.component1() = x
operator fun Vector3dc.component2() = y
operator fun Vector3dc.component3() = z

val Vector3dc.x get() = x()
val Vector3dc.y get() = y()
val Vector3dc.z get() = z()

fun Vector3dc.multiplyTerms() = x * y * z
fun Vector3dc.addTerms() = x + y + z
fun Vector3dc.horizontalLengthSq() = x * x + z * z

fun Vector3dc.differenceHorLengthSq(other: Vector3dc): Double {
    val xDif = x() - other.x()
    val zDif = z() - other.z()
    return xDif * xDif + zDif * zDif
}

fun Vector3dc.horizontalLength() = kotlin.math.sqrt(horizontalLengthSq())

// Matrix4dc

// Matrix3dc

// endregion

// region GDX

// Vector3

// Matrix4

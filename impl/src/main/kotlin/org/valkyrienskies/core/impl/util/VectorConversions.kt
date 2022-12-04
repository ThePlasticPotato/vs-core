package org.valkyrienskies.core.impl.util

import org.joml.Vector3dc
import org.joml.Vector3ic
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBi
import org.joml.primitives.AABBic
import kotlin.math.round

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

// AABBdc
fun AABBdc.toAABBi(dest: AABBi = AABBi()): AABBi {
    dest.setMin(round(minX()).toInt(), round(minY()).toInt(), round(minZ()).toInt())
    dest.setMax(round(maxX()).toInt(), round(maxY()).toInt(), round(maxZ()).toInt())
    return dest
}

// AABBic
fun AABBic.expand(expansion: Int, dest: AABBi = AABBi()): AABBi {
    dest.minX = minX() - expansion
    dest.minY = minY() - expansion
    dest.minZ = minZ() - expansion
    dest.maxX = maxX() + expansion
    dest.maxY = maxY() + expansion
    dest.maxZ = maxZ() + expansion
    return dest
}

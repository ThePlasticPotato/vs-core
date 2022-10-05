package org.valkyrienskies.core.util

import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun AABBd.set(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): AABBd = also {
    setMin(minX, minY, minZ)
    setMax(maxX, maxY, maxZ)
}

fun AABBd.expand(expansion: Double): AABBd {
    minX -= expansion
    minY -= expansion
    minZ -= expansion
    maxX += expansion
    maxY += expansion
    maxZ += expansion
    return this
}

fun AABBd.getSize(): Double {
    val d: Double = getXsize()
    val e: Double = getYsize()
    val f: Double = getZsize()
    return (d + e + f) / 3.0
}

fun AABBd.getXsize(): Double {
    return maxX - minX
}

fun AABBd.getYsize(): Double {
    return maxY - minY
}

fun AABBd.getZsize(): Double {
    return maxZ - minZ
}

fun AABBd.extend(extension: Vector3dc): AABBd {
    if (extension.x() > 0) maxX += extension.x() else minX += extension.x()
    if (extension.y() > 0) maxY += extension.y() else minY += extension.y()
    if (extension.z() > 0) maxZ += extension.z() else minZ += extension.z()
    return this
}

fun AABBdc.signedDistanceTo(pos: Vector3dc): Double {
    val isPointInside = containsPoint(pos)
    if (isPointInside) {
        val centerX = (minX() + maxX()) / 2.0
        val centerY = (minY() + maxY()) / 2.0
        val centerZ = (minZ() + maxZ()) / 2.0

        // The AABB extends from [-lenX, -lenY, -lenZ] to [lenX, lenY, lenZ]
        val lenX = (maxX() - minX()) / 2.0
        val lenY = (maxX() - minX()) / 2.0
        val lenZ = (maxX() - minX()) / 2.0

        val relPosX = pos.x() - centerX
        val relPosY = pos.y() - centerY
        val relPosZ = pos.z() - centerZ

        val xDist = lenX - abs(relPosX) // Dist to x-side
        val yDist = lenY - abs(relPosY) // Dist to y-side
        val zDist = lenZ - abs(relPosZ) // Dist to z-side
        return -min(xDist, min(yDist, zDist)) // Make minDistToSides negative because we are inside the AABB
    } else {
        val closestSurfacePointX = max(minX(), min(maxX(), pos.x()))
        val closestSurfacePointY = max(minY(), min(maxY(), pos.y()))
        val closestSurfacePointZ = max(minZ(), min(maxZ(), pos.z()))
        return pos.distance(closestSurfacePointX, closestSurfacePointY, closestSurfacePointZ)
    }
}

fun AABBic.toAABBd(dest: AABBd): AABBd {
    dest.minX = minX().toDouble()
    dest.minY = minY().toDouble()
    dest.minZ = minZ().toDouble()
    dest.maxX = maxX().toDouble()
    dest.maxY = maxY().toDouble()
    dest.maxZ = maxZ().toDouble()
    return dest
}

fun AABBdc.intersectsAABB(other: AABBdc) = intersectsAABB(other as AABBd)

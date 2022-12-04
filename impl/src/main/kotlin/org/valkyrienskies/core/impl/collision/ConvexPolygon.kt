package org.valkyrienskies.core.impl.collision

import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * A convex polygon consists of points and normals.
 *
 * Note that we do not assign a particular normal for each point, because we do not need it for SAT collision.
 */
open class ConvexPolygon constructor(
    private val _points: List<Vector3dc>,
    private val _normals: List<Vector3dc>,
    private val _shipFrom: ShipId? = null,
    private val _aabb: AABBdc
) : ConvexPolygonc {
    override val points: Iterable<Vector3dc> get() = _points
    override val normals: Iterable<Vector3dc> get() = _normals
    override val shipFrom: ShipId? get() = _shipFrom
    override val aabb: AABBdc get() = _aabb

    companion object {
        fun createFromPointsAndNormals(points: List<Vector3dc>, normals: List<Vector3dc>, aabb: AABBdc): ConvexPolygon {
            return ConvexPolygon(points, normals, null, aabb)
        }
    }
}

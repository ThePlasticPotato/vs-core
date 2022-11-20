package org.valkyrienskies.core.api.ships.properties

import org.joml.Matrix4dc
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBdc


interface ShipTransform {
    val shipPositionInWorldCoordinates: Vector3dc
    val shipPositionInShipCoordinates: Vector3dc
    val shipCoordinatesToWorldCoordinatesRotation: Quaterniondc
    val shipCoordinatesToWorldCoordinatesScaling: Vector3dc

    /**
     * Transforms positions and directions from ship coordinates to world coordinates
     */
    val shipToWorldMatrix: Matrix4dc

    /**
     * Transforms positions and directions from world coordinates to ships coordinates
     */
    val worldToShipMatrix: Matrix4dc
    fun transformDirectionNoScalingFromShipToWorld(directionInShip: Vector3dc, dest: Vector3d): Vector3d
    fun transformDirectionNoScalingFromWorldToShip(directionInWorld: Vector3dc, dest: Vector3d): Vector3d

    /**
     * Create an empty [AABBdc] centered around [shipPositionInWorldCoordinates].
     */
    fun createEmptyAABB(): AABBdc
}

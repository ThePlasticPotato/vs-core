package org.valkyrienskies.core.api.ships.properties

import org.joml.Matrix4dc
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBdc


interface ShipTransform {

    val positionInWorld: Vector3dc
    val positionInShip: Vector3dc
    val shipToWorldRotation: Quaterniondc
    val shipToWorldScaling: Vector3dc

    /**
     * Transforms positions and directions from ship coordinates to world coordinates
     */
    val shipToWorld: Matrix4dc

    /**
     * Transforms positions and directions from world coordinates to ships coordinates
     */
    val worldToShip: Matrix4dc
    fun transformDirectionNoScalingFromShipToWorld(directionInShip: Vector3dc, dest: Vector3d): Vector3d
    fun transformDirectionNoScalingFromWorldToShip(directionInWorld: Vector3dc, dest: Vector3d): Vector3d

    /**
     * Create an empty [AABBdc] centered around [positionInWorld].
     */
    fun createEmptyAABB(): AABBdc
    
    @Deprecated("renamed", ReplaceWith("positionInWorld"))
    val shipPositionInWorldCoordinates: Vector3dc get() = positionInWorld

    @Deprecated("renamed", ReplaceWith("positionInShip"))
    val shipPositionInShipCoordinates: Vector3dc get() = positionInShip

    @Deprecated("renamed", ReplaceWith("shipToWorldRotation"))
    val shipCoordinatesToWorldCoordinatesRotation: Quaterniondc get() = shipToWorldRotation

    @Deprecated("renamed", ReplaceWith("shipToWorldScaling"))
    val shipCoordinatesToWorldCoordinatesScaling: Vector3dc get() = shipToWorldScaling

    @Deprecated("renamed", ReplaceWith("shipToWorld"))
    val shipToWorldMatrix: Matrix4dc get() = shipToWorld

    @Deprecated("renamed", ReplaceWith("worldToShip"))
    val worldToShipMatrix: Matrix4dc get() = worldToShip

}

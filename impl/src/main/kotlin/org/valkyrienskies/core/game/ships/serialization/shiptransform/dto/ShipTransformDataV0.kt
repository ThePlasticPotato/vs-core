package org.valkyrienskies.core.game.ships.serialization.shiptransform.dto

import org.joml.Matrix4d
import org.joml.Matrix4dc
import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc

data class ShipTransformDataV0(
    val shipPositionInWorldCoordinates: Vector3dc,
    val shipPositionInShipCoordinates: Vector3dc,
    val shipCoordinatesToWorldCoordinatesRotation: Quaterniondc,
    val shipCoordinatesToWorldCoordinatesScaling: Vector3dc,
) {
    /**
     * Transforms positions and directions from ship coordinates to world coordinates
     */
    val shipToWorldMatrix: Matrix4dc

    /**
     * Transforms positions and directions from world coordinates to ships coordinates
     */
    val worldToShipMatrix: Matrix4dc

    init {
        shipToWorldMatrix = Matrix4d()
            .translate(shipPositionInWorldCoordinates)
            .rotate(shipCoordinatesToWorldCoordinatesRotation)
            .scale(shipCoordinatesToWorldCoordinatesScaling)
            .translate(
                -shipPositionInShipCoordinates.x(),
                -shipPositionInShipCoordinates.y(),
                -shipPositionInShipCoordinates.z()
            )
        worldToShipMatrix = shipToWorldMatrix.invert(Matrix4d())
    }

    fun createEmptyAABB(): AABBdc {
        return AABBd(shipPositionInWorldCoordinates, shipPositionInWorldCoordinates)
    }
}
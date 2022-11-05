package org.valkyrienskies.core.game.ships

import org.joml.Matrix4d
import org.joml.Matrix4dc
import org.joml.Quaterniond
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc

/**
 * The [ShipTransform] class is responsible for transforming position and direction vectors between ship coordinates and world coordinates.
 *
 * The process of transforming a position transformation from ship coordinates to world coordinates is defined as the following:
 *
 * First it is translated by -[shipPositionInShipCoordinates],
 * then it is scaled by [shipCoordinatesToWorldCoordinatesScaling],
 * after that it is rotated by [shipCoordinatesToWorldCoordinatesRotation],
 * finally it is translated by [shipPositionInWorldCoordinates].
 */
data class ShipTransform(
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

    companion object {
        // The quaternion that represents no rotation
        private val ZERO_ROTATION: Quaterniondc = Quaterniond()

        // The vector that represents no scaling
        private val UNIT_SCALING: Vector3dc = Vector3d(1.0, 1.0, 1.0)

        private val ZERO: Vector3dc = Vector3d()

        fun createEmpty(): ShipTransform {
            return createFromCoordinates(ZERO, ZERO)
        }

        fun createFromCoordinates(
            centerCoordinateInWorld: Vector3dc,
            centerCoordinateInShip: Vector3dc
        ): ShipTransform {
            return createFromCoordinatesAndRotation(centerCoordinateInWorld, centerCoordinateInShip, ZERO_ROTATION)
        }

        fun createFromCoordinatesAndRotation(
            centerCoordinateInWorld: Vector3dc,
            centerCoordinateInShip: Vector3dc,
            shipRotation: Quaterniondc
        ): ShipTransform {
            return ShipTransform(centerCoordinateInWorld, centerCoordinateInShip, shipRotation, UNIT_SCALING)
        }

        fun createFromCoordinatesAndRotationAndScaling(
            centerCoordinateInWorld: Vector3dc,
            centerCoordinateInShip: Vector3dc,
            shipRotation: Quaterniondc,
            shipScaling: Vector3dc
        ): ShipTransform {
            return ShipTransform(centerCoordinateInWorld, centerCoordinateInShip, shipRotation, shipScaling)
        }

        /**
         * Interpolate between two [ShipTransform] based on [alpha]
         * @param alpha Must be between 0 and 1 inclusive
         */
        fun createFromSlerp(prevTransform: ShipTransform, curTransform: ShipTransform, alpha: Double): ShipTransform {
            // Always use the center coord from the new transform
            val newCenterCoords = curTransform.shipPositionInShipCoordinates

            val centerCoordDif =
                curTransform.shipPositionInShipCoordinates.sub(prevTransform.shipPositionInShipCoordinates, Vector3d())

            val oldWorldPosWithRespectToNewCenter = prevTransform.shipToWorldMatrix.transformDirection(
                centerCoordDif, Vector3d()
            ).add(prevTransform.shipPositionInWorldCoordinates)

            val newWorldCoords = oldWorldPosWithRespectToNewCenter.lerp(
                curTransform.shipPositionInWorldCoordinates,
                alpha,
                Vector3d()
            )

            val newRotation = prevTransform.shipCoordinatesToWorldCoordinatesRotation.slerp(
                curTransform.shipCoordinatesToWorldCoordinatesRotation,
                alpha,
                Quaterniond()
            ).normalize()

            val newScaling = prevTransform.shipCoordinatesToWorldCoordinatesScaling.lerp(
                curTransform.shipCoordinatesToWorldCoordinatesScaling,
                alpha,
                Vector3d()
            )

            return createFromCoordinatesAndRotationAndScaling(
                newWorldCoords,
                newCenterCoords,
                newRotation,
                newScaling
            )
        }
    }

    fun transformDirectionNoScalingFromShipToWorld(directionInShip: Vector3dc, dest: Vector3d): Vector3d {
        return shipCoordinatesToWorldCoordinatesRotation.transform(directionInShip, dest)
    }

    fun transformDirectionNoScalingFromWorldToShip(directionInWorld: Vector3dc, dest: Vector3d): Vector3d {
        return shipCoordinatesToWorldCoordinatesRotation.transformInverse(directionInWorld, dest)
    }

    /**
     * Create an empty [AABBdc] centered around [shipPositionInWorldCoordinates].
     */
    fun createEmptyAABB(): AABBdc {
        return AABBd(shipPositionInWorldCoordinates, shipPositionInWorldCoordinates)
    }
}

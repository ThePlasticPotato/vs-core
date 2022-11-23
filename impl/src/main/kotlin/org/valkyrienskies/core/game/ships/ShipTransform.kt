package org.valkyrienskies.core.game.ships

import org.joml.*
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipTransform

/**
 * The [ShipTransform] class is responsible for transforming position and direction vectors between ship coordinates and world coordinates.
 *
 * The process of transforming a position transformation from ship coordinates to world coordinates is defined as the following:
 *
 * First it is translated by -[positionInShip],
 * then it is scaled by [shipToWorldScaling],
 * after that it is rotated by [shipToWorldRotation],
 * finally it is translated by [positionInWorld].
 */
data class ShipTransformImpl(
    override val positionInWorld: Vector3dc,
    override val positionInShip: Vector3dc,
    override val shipToWorldRotation: Quaterniondc,
    override val shipToWorldScaling: Vector3dc,
) : ShipTransform {

    /**
     * Transforms positions and directions from ship coordinates to world coordinates
     */
    override val shipToWorld: Matrix4dc

    /**
     * Transforms positions and directions from world coordinates to ships coordinates
     */
    override val worldToShip: Matrix4dc

    init {
        shipToWorld = Matrix4d()
            .translate(positionInWorld)
            .rotate(shipToWorldRotation)
            .scale(shipToWorldScaling)
            .translate(
                -positionInShip.x(),
                -positionInShip.y(),
                -positionInShip.z()
            )
        worldToShip = shipToWorld.invert(Matrix4d())
    }

    override fun transformDirectionNoScalingFromShipToWorld(directionInShip: Vector3dc, dest: Vector3d): Vector3d {
        return shipToWorldRotation.transform(directionInShip, dest)
    }

    override fun transformDirectionNoScalingFromWorldToShip(directionInWorld: Vector3dc, dest: Vector3d): Vector3d {
        return shipToWorldRotation.transformInverse(directionInWorld, dest)
    }

    /**
     * Create an empty [AABBdc] centered around [positionInWorld].
     */
    override fun createEmptyAABB(): AABBdc {
        return AABBd(positionInWorld, positionInWorld)
    }

    companion object {

        // The quaternion that represents no rotation
        val ZERO_ROTATION: Quaterniondc = Quaterniond()

        // The vector that represents no scaling
        val UNIT_SCALING: Vector3dc = Vector3d(1.0, 1.0, 1.0)

        val ZERO: Vector3dc = Vector3d()

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
            return ShipTransformImpl(
                centerCoordinateInWorld,
                centerCoordinateInShip,
                shipRotation,
                UNIT_SCALING
            )
        }

        fun createFromCoordinatesAndRotationAndScaling(
            centerCoordinateInWorld: Vector3dc,
            centerCoordinateInShip: Vector3dc,
            shipRotation: Quaterniondc,
            shipScaling: Vector3dc
        ): ShipTransform {
            return ShipTransformImpl(
                centerCoordinateInWorld,
                centerCoordinateInShip,
                shipRotation,
                shipScaling
            )
        }

        /**
         * Interpolate between two [ShipTransform] based on [alpha]
         * @param alpha Must be between 0 and 1 inclusive
         */
        fun createFromSlerp(prevTransform: ShipTransform, curTransform: ShipTransform, alpha: Double): ShipTransform {
            // Always use the center coord from the new transform
            val newCenterCoords = curTransform.positionInShip

            val centerCoordDif =
                curTransform.positionInShip.sub(prevTransform.positionInShip, Vector3d())

            val oldWorldPosWithRespectToNewCenter = prevTransform.shipToWorld.transformDirection(
                centerCoordDif, Vector3d()
            ).add(prevTransform.positionInWorld)

            val newWorldCoords = oldWorldPosWithRespectToNewCenter.lerp(
                curTransform.positionInWorld,
                alpha,
                Vector3d()
            )

            val newRotation = prevTransform.shipToWorldRotation.slerp(
                curTransform.shipToWorldRotation,
                alpha,
                Quaterniond()
            ).normalize()

            val newScaling = prevTransform.shipToWorldScaling.lerp(
                curTransform.shipToWorldScaling,
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
}

package org.valkyrienskies.core.impl.game.ships

import org.joml.*
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipTransform

/**
 * The [ShipTransform] class is responsible for transforming position and direction vectors between ship coordinates and world coordinates.
 *
 * The process of transforming a position transformation from ship coordinates to world coordinates is defined as the following:
 *
 * First it is translated by -[positionInModel],
 * then it is scaled by [scaling],
 * after that it is rotated by [rotation],
 * finally it is translated by [position].
 */
data class ShipTransformImpl(
    override val position: Vector3dc,
    override val positionInModel: Vector3dc,
    override val rotation: Quaterniondc,
    override val scaling: Vector3dc
) : ShipTransform {

    /**
     * Transforms positions and directions from ship coordinates to world coordinates
     */
    override val toWorld: Matrix4dc

    /**
     * Transforms positions and directions from world coordinates to ships coordinates
     */
    override val toModel: Matrix4dc

    init {
        toWorld = Matrix4d()
            .translate(position)
            .rotate(rotation)
            .scale(scaling)
            .translate(
                -positionInModel.x(),
                -positionInModel.y(),
                -positionInModel.z()
            )
        toModel = toWorld.invert(Matrix4d())
    }

    override fun transformDirectionNoScalingFromShipToWorld(directionInShip: Vector3dc, dest: Vector3d): Vector3d {
        return rotation.transform(directionInShip, dest)
    }

    override fun transformDirectionNoScalingFromWorldToShip(directionInWorld: Vector3dc, dest: Vector3d): Vector3d {
        return rotation.transformInverse(directionInWorld, dest)
    }

    /**
     * Create an empty [AABBdc] centered around [position].
     */
    override fun createEmptyAABB(): AABBdc {
        return AABBd(position, position)
    }

    companion object {

        // The quaternion that represents no rotation
        val ZERO_ROTATION: Quaterniondc = Quaterniond()

        // The vector that represents no scaling
        val UNIT_SCALING: Vector3dc = Vector3d(1.0, 1.0, 1.0)

        val ZERO: Vector3dc = Vector3d()

        fun createEmpty(): ShipTransform {
            return create(ZERO, ZERO)
        }

        fun create(
            centerCoordinateInWorld: Vector3dc,
            centerCoordinateInShip: Vector3dc
        ): ShipTransform {
            return create(centerCoordinateInWorld, centerCoordinateInShip, ZERO_ROTATION)
        }

        fun create(
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

        fun create(
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
            val newCenterCoords = curTransform.positionInModel

            val centerCoordDif =
                curTransform.positionInModel.sub(prevTransform.positionInModel, Vector3d())

            val oldWorldPosWithRespectToNewCenter = prevTransform.toWorld.transformDirection(
                centerCoordDif, Vector3d()
            ).add(prevTransform.position)

            val newWorldCoords = oldWorldPosWithRespectToNewCenter.lerp(
                curTransform.position,
                alpha,
                Vector3d()
            )

            val newRotation = prevTransform.rotation.slerp(
                curTransform.rotation,
                alpha,
                Quaterniond()
            ).normalize()

            val newScaling = prevTransform.scaling.lerp(
                curTransform.scaling,
                alpha,
                Vector3d()
            )

            return create(
                newWorldCoords,
                newCenterCoords,
                newRotation,
                newScaling
            )
        }
    }
}

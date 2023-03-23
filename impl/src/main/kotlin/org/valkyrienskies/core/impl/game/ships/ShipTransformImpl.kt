package org.valkyrienskies.core.impl.game.ships

import org.joml.*
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.bodies.properties.BodyTransform
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
open class ShipTransformImpl(
    override val position: Vector3dc,
    override val positionInModel: Vector3dc,
    override val rotation: Quaterniondc,
    override val scaling: Vector3dc,
    /**
     * Transforms positions and directions from ship coordinates to world coordinates
     */
    override val toWorld: Matrix4dc = Matrix4d()
        .translate(position)
        .rotate(rotation)
        .scale(scaling)
        .translate(
            -positionInModel.x(),
            -positionInModel.y(),
            -positionInModel.z()
        ),

    /**
     * Transforms positions and directions from world coordinates to ships coordinates
     */
    override val toModel: Matrix4dc = toWorld.invert(Matrix4d())
) : ShipTransform {

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShipTransformImpl

        if (position != other.position) return false
        if (positionInModel != other.positionInModel) return false
        if (rotation != other.rotation) return false
        if (scaling != other.scaling) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + positionInModel.hashCode()
        result = 31 * result + rotation.hashCode()
        result = 31 * result + scaling.hashCode()
        return result
    }

    override fun toString(): String {
        return "ShipTransformImpl(position=$position, positionInModel=$positionInModel, rotation=$rotation, scaling=$scaling)"
    }

    companion object {

        // The quaternion that represents no rotation
        val ZERO_ROTATION: Quaterniondc = Quaterniond()

        // The vector that represents no scaling
        val UNIT_SCALING: Vector3dc = Vector3d(1.0, 1.0, 1.0)

        val ZERO: Vector3dc = Vector3d()

        fun create(previous: BodyTransform, toWorld: Matrix4dc): ShipTransform {
            val toWorld = Matrix4d(toWorld)
            toWorld.determineProperties()

            require(toWorld.isAffine) { "Body transform must be an affine matrix - no skew!" }

            val positionInModel = previous.positionInModel
            val position = toWorld.transformPosition(Vector3d(positionInModel))
            val rotation = toWorld.getNormalizedRotation(Quaterniond())
            val scaling = toWorld.getScale(Vector3d())

            return ShipTransformImpl(position, positionInModel, rotation, scaling, toWorld)
        }

        fun createWithPositionInModel(previous: BodyTransform, positionInModel: Vector3dc): ShipTransform {
            val newPosition = Vector3d(previous.positionInModel)
                .sub(positionInModel)
                .add(previous.position)

            return ShipTransformImpl(newPosition, positionInModel, previous.rotation, previous.scaling)
        }

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

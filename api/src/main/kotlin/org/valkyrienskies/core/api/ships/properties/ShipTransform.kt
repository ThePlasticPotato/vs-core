package org.valkyrienskies.core.api.ships.properties

import org.joml.Matrix4dc
import org.joml.Quaterniondc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.bodies.properties.EntityTransform


@Deprecated("renamed", ReplaceWith("EntityTransform", "org.valkyrienskies.core.api.vsentities.properties.EntityTransform"))
interface ShipTransform : EntityTransform {

    fun transformDirectionNoScalingFromShipToWorld(directionInShip: Vector3dc, dest: Vector3d): Vector3d
    fun transformDirectionNoScalingFromWorldToShip(directionInWorld: Vector3dc, dest: Vector3d): Vector3d

    /**
     * Create an empty [AABBdc] centered around [position].
     */
    fun createEmptyAABB(): AABBdc

    @Deprecated("renamed", ReplaceWith("positionInModel"))
    val positionInShip: Vector3dc get() = positionInModel

    @Deprecated("renamed", ReplaceWith("rotation"))
    val shipToWorldRotation: Quaterniondc get() = rotation

    @Deprecated("renamed", ReplaceWith("scaling"))
    val shipToWorldScaling: Vector3dc get() = scaling

    @Deprecated("renamed", ReplaceWith("toWorld"))
    val shipToWorld: Matrix4dc get() = toWorld

    @Deprecated("renamed", ReplaceWith("toModel"))
    val worldToShip: Matrix4dc get() = toModel

    @Deprecated("renamed", ReplaceWith("position"))
    val positionInWorld: Vector3dc get() = position

    @Deprecated("renamed", ReplaceWith("position"))
    val shipPositionInWorldCoordinates: Vector3dc get() = position

    @Deprecated("renamed", ReplaceWith("positionInModel"))
    val shipPositionInShipCoordinates: Vector3dc get() = positionInModel

    @Deprecated("renamed", ReplaceWith("rotation"))
    val shipCoordinatesToWorldCoordinatesRotation: Quaterniondc get() = rotation

    @Deprecated("renamed", ReplaceWith("scaling"))
    val shipCoordinatesToWorldCoordinatesScaling: Vector3dc get() = scaling

    @Deprecated("renamed", ReplaceWith("toWorld"))
    val shipToWorldMatrix: Matrix4dc get() = toWorld

    @Deprecated("renamed", ReplaceWith("toModel"))
    val worldToShipMatrix: Matrix4dc get() = toModel

}

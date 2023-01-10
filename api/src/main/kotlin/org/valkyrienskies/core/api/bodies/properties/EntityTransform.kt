package org.valkyrienskies.core.api.bodies.properties

import org.joml.Matrix4dc
import org.joml.Quaterniondc
import org.joml.Vector3dc

interface EntityTransform {

    /**
     * The position of the entity in world-space
     */
    val position: Vector3dc

    /**
     * The position of the entity in model-space
     *
     * On a ship, this is the position of the ship in the shipyard
     */
    val positionInModel: Vector3dc

    /**
     * The rotation of the entity, from model to world-space
     */
    val rotation: Quaterniondc

    /**
     * The scaling of the entity, from model to world-space
     */
    val scaling: Vector3dc

    /**
     * Transforms coordinates from model-space to world-space
     *
     * On a ship, this transforms coordinates from the shipyard to world-space
     */
    val toWorld: Matrix4dc

    /**
     * Transforms coordinates from world-space to model-space
     *
     * On a ship, this transforms coordinates from world-space to the shipyard
     */
    val toModel: Matrix4dc
}
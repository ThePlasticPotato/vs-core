package org.valkyrienskies.core.ecs.components

import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.valkyrienskies.core.ecs.Component

data class WorldTransformComponent(
    val shipPositionInWorldCoordinates: Vector3dc,
    val shipCoordinatesToWorldCoordinatesRotation: Quaterniondc,
    val shipCoordinatesToWorldCoordinatesScaling: Vector3dc,
) : Component

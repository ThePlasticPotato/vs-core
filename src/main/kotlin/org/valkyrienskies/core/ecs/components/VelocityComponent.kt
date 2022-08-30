package org.valkyrienskies.core.ecs.components

import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.ecs.Component
import org.valkyrienskies.core.ecs.default

data class VelocityComponent(
    var linearVelocity: Vector3dc,
    var angularVelocity: Vector3dc
) : Component

private val DEFAULT = VelocityComponent::class.default { VelocityComponent(Vector3d(), Vector3d()) }

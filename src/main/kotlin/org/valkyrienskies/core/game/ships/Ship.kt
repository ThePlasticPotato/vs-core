package org.valkyrienskies.core.game.ships

import org.valkyrienskies.core.ecs.components.ChunkClaimComponent
import org.valkyrienskies.core.ecs.components.InertiaComponent
import org.valkyrienskies.core.ecs.components.VelocityComponent
import org.valkyrienskies.core.ecs.components.WorldTransformComponent
import org.valkyrienskies.core.ecs.compose

class Ship {

    companion object {
        val COMPOSITION = compose {
            +WorldTransformComponent::class
            +ChunkClaimComponent::class
            +VelocityComponent::class
            +InertiaComponent::class
        }
    }
}

package org.valkyrienskies.core.ecs

import io.kotest.core.spec.style.StringSpec
import org.valkyrienskies.core.ecs.components.ChunkClaimComponent
import org.valkyrienskies.core.ecs.tracking.VentityChunkTracker
import org.valkyrienskies.core.game.Dimensions

class ChunkTracker : StringSpec({

    "initialize chunktracker" {
        val world = VSWorld()
        val chunkTracker = VentityChunkTracker(world)
        assert(!chunkTracker.iterator().hasNext())
    }

    "add ship and iterate" {
        val world = VSWorld()
        val chunkTracker = VentityChunkTracker(world)

        world.spawn("testShip", ChunkClaimComponent(0, 0, Dimensions.OVERWORLD))

        assert(chunkTracker.iterator().hasNext())
    }

    "add ship and find it with pos" {
        val world = VSWorld()
        val chunkTracker = VentityChunkTracker(world)

        world.spawn("testShip", ChunkClaimComponent(0, 0, Dimensions.OVERWORLD))
        
        assert(chunkTracker.getVentityFromPos(0, 0, Dimensions.OVERWORLD) != null)
    }
})

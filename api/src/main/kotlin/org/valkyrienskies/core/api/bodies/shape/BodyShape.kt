package org.valkyrienskies.core.api.bodies.shape

import org.joml.Vector3dc
import org.joml.primitives.AABBic

interface BodyShape {

    interface Sphere {
        val radius: Double
    }

    interface Box {
        val lengths: Vector3dc
    }

    interface Wheel {
        val radius: Double
        val halfThickness: Double
    }

    interface Capsule {
        val radius: Double
        val halfLength: Double
    }

    interface Voxel {
        val definedArea: AABBic

        /**
         * Applies a VoxelUpdate async. This may be called from any thread.
         */
        fun applyUpdateAsync(update: VoxelUpdate)

        /**
         * (not recommended)
         *
         * Applies a VoxelUpdate sync. This may only be called from the physics thread. However,
         * it is preferred to use [applyUpdateAsync] from the physics thread as well if you can, as
         * applying dense updates synchronously may lag the physics tick.
         */
        fun applyUpdateSync(update: VoxelUpdate)
    }
}
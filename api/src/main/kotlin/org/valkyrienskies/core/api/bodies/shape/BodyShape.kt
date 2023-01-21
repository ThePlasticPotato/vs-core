package org.valkyrienskies.core.api.bodies.shape

import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic

interface BodyShape {

    val aabb: AABBdc

    /**
     * A sphere centered at 0, 0, 0 with [radius]
     */
    interface Sphere : BodyShape {
        val radius: Double
    }

    /**
     * A box centered at 0, 0, 0. Its half-lengths are determined by [halfLengths] - the length of each side is double the half-length.
     */
    interface Box : BodyShape {
        val halfLengths: Vector3dc
    }

    /**
     * A wheel centered at 0, 0, 0 in the yz plane
     */
    interface Wheel : BodyShape {
        val radius: Double
        val halfThickness: Double
    }

    /**
     * A capsule centered at 0, 0, 0 along the x-axis
     */
    interface Capsule : BodyShape {
        val radius: Double
        val halfLength: Double
    }

    interface Voxel : BodyShape {
        /**
         * The area over which the voxel shape is defined. Chunks outside of this area will be
         * considered air by default, rather than unloaded
         */
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

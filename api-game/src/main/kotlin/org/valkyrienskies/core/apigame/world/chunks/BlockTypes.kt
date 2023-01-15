package org.valkyrienskies.core.apigame.world.chunks

import org.valkyrienskies.core.api.bodies.shape.VoxelTypes

@Deprecated("moved to api", ReplaceWith("VoxelTypes", "org.valkyrienskies.core.api.bodies.shape.VoxelTypes"))
interface BlockTypes : VoxelTypes {

    override val air: BlockType
    override val solid: BlockType
    override val lava: BlockType
    override val water: BlockType
}

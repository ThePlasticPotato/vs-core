package org.valkyrienskies.core.apigame.world.chunks

@Deprecated("moved to api", ReplaceWith("org.valkyrienskies.core.api.bodies.shape.BlockType"))
interface BlockType : org.valkyrienskies.core.api.bodies.shape.VoxelType {
    @Deprecated(
        "This is a code smell... API consumers should not have to worry about " +
            "how block states are internally represented"
    )
    fun toByte(): Byte
}

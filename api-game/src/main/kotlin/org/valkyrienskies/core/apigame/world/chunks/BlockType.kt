package org.valkyrienskies.core.apigame.world.chunks

interface BlockType {
    @Deprecated(
        "This is a code smell... API consumers should not have to worry about " +
            "how block states are internally represented"
    )
    fun toInt(): Int
}

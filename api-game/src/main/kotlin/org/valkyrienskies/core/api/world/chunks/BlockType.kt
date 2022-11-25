package org.valkyrienskies.core.api.world.chunks

interface BlockType {
    @Deprecated(
        "This is a code smell... API consumers should not have to worry about " +
            "how block states are internally represented"
    )
    fun toByte(): Byte
}

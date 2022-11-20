package org.valkyrienskies.core.api.ships.properties

interface VSBlockType {
    @Deprecated("This is a code smell... API consumers should not have to worry about " +
        "how block states are internally represented")
    fun toByte(): Byte
}

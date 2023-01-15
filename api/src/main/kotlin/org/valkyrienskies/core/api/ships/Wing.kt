package org.valkyrienskies.core.api.ships

import org.joml.Vector3dc

data class Wing(
    val wingNormal: Vector3dc,
    val wingLiftPower: Double,
    val wingDragPower: Double,

    /**
     * Null if infinite
     */
    val wingBreakingForce: Double?
)

data class PositionedWing(val posX: Int, val posY: Int, val posZ: Int, val wing: Wing?)

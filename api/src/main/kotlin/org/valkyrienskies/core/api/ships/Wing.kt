package org.valkyrienskies.core.api.ships

import org.joml.Vector3dc
import org.joml.Vector3ic

data class Wing(
    val wingNormal: Vector3dc,
    val wingLiftPower: Double,
    val wingDragPower: Double,

    /**
     * Null if infinite
     */
    val wingBreakingForce: Double?
)

data class PositionedWing(val pos: Vector3ic, val wing: Wing)

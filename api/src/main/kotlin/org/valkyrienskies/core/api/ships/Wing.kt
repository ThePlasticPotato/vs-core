package org.valkyrienskies.core.api.ships

import org.joml.Vector3dc

data class Wing(
    val wingNormal: Vector3dc,
    val wingLiftPower: Double,
    val wingDragPower: Double,

    /**
     * Null if infinite
     */
    val wingBreakingForce: Double?,

    // Angle of airfoil bias in radians, typically zero for symmetric airfoils, and non-zero for cambered airfoils
    //
    // Note that positive values will make lift in the direction of [wingNormal] at 0 angle of attack
    val wingCamberAttackAngleBias: Double
)

data class PositionedWing(val posX: Int, val posY: Int, val posZ: Int, val wing: Wing?)

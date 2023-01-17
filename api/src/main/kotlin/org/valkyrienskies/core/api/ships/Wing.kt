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
    val wingCamberedBiasAngle: Double
)

data class PositionedWing(val posX: Int, val posY: Int, val posZ: Int, val wing: Wing?)

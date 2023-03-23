package org.valkyrienskies.core.impl.entities.bodies

data class BodySettings(
    val isStatic: Boolean,
    val buoyantFactor: Double,
    val doFluidDrag: Boolean
)
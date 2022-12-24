package org.valkyrienskies.core.apigame.constraints

import org.joml.Quaterniondc

/**
 * A constraint that applies a torque on two bodies.
 */
interface VSTorqueConstraint : VSConstraint {
    // The local rotation of the constraint in body0
    val localRot0: Quaterniondc

    // The local rotation of the constraint in body1
    val localRot1: Quaterniondc

    // The maximum torque this constraint can tolerate before it breaks
    val maxTorque: Double
}

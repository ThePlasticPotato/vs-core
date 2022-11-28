package org.valkyrienskies.core.game.ships

import org.valkyrienskies.core.api.ShipForcesInducer
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.RigidBodyReference
import org.valkyrienskies.physics_api.SegmentTracker

data class PhysShip internal constructor(
    val shipId: ShipId,
    // Don't use these outside of vs-core, I beg of thee
    internal val rigidBodyReference: RigidBodyReference,
    internal var forceInducers: List<ShipForcesInducer>,
    internal var _inertia: PhysInertia,

    // TODO transformation matrix
    var poseVel: PoseVel,
    var segments: SegmentTracker
) {
    var buoyantFactor by rigidBodyReference::buoyantFactor
    var doFluidDrag by rigidBodyReference::doFluidDrag

    val inertia: PhysInertia
        get() = _inertia
}

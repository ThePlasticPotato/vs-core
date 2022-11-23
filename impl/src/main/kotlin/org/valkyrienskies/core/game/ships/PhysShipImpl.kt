package org.valkyrienskies.core.game.ships

import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.attachments.ShipForcesInducer
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.util.assertions.assertIsPhysicsThread
import org.valkyrienskies.core.util.assertions.requireIsFinite
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.RigidBodyReference
import org.valkyrienskies.physics_api.SegmentTracker

data class PhysShipImpl internal constructor(
    override val id: ShipId,
    // Don't use these outside of vs-core, I beg of thee
    internal val rigidBodyReference: RigidBodyReference,
    internal var forceInducers: List<ShipForcesInducer>,
    internal var _inertia: PhysInertia,

    // TODO transformation matrix
    var poseVel: PoseVel,
    var segments: SegmentTracker
) : PhysShip {
    override var buoyantFactor by rigidBodyReference::buoyantFactor

    val inertia: PhysInertia
        get() = _inertia


    private val invForces = ArrayDeque<Vector3dc>()
    private val invTorques = ArrayDeque<Vector3dc>()
    private val rotForces = ArrayDeque<Vector3dc>()
    private val rotTorques = ArrayDeque<Vector3dc>()
    private val invPosForces = ArrayDeque<Vector3dc>()
    private val invPosPositions = ArrayDeque<Vector3dc>()

    override var isStatic = false

    fun applyQueuedForces() {
        invForces.removeIf { i -> rigidBodyReference.addInvariantForceToNextPhysTick(i); true }
        invTorques.removeIf { i -> rigidBodyReference.addInvariantTorqueToNextPhysTick(i); true }
        rotForces.removeIf { i -> rigidBodyReference.addRotDependentForceToNextPhysTick(i); true }
        rotTorques.removeIf { i -> rigidBodyReference.addRotDependentTorqueToNextPhysTick(i); true }

        for ((index, force) in invPosForces.withIndex()) {
            rigidBodyReference.addInvariantForceAtPosToNextPhysTick(force, invPosPositions.elementAt(index))
        }
        rigidBodyReference.isStatic = isStatic
    }

    override fun applyInvariantForce(force: Vector3dc) {
        requireIsFinite(force)
        assertIsPhysicsThread()

        invForces.add(force)
    }

    override fun applyInvariantTorque(torque: Vector3dc) {
        requireIsFinite(torque)
        assertIsPhysicsThread()

        invForces.add(torque)
    }

    override fun applyRotDependentForce(force: Vector3dc) {
        requireIsFinite(force)
        assertIsPhysicsThread()

        invForces.add(force)
    }

    override fun applyRotDependentTorque(torque: Vector3dc) {
        requireIsFinite(torque)
        assertIsPhysicsThread()

        invForces.add(torque)
    }

    override fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc) {
        requireIsFinite(force)
        requireIsFinite(pos)
        assertIsPhysicsThread()

        invPosForces.add(force)
        invPosPositions.add(pos)
    }

}
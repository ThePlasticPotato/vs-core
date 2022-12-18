package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.api.ShipForcesInducer
import org.valkyrienskies.core.impl.util.assertions.assertIsPhysicsThread
import org.valkyrienskies.core.impl.util.assertions.requireIsFinite
import org.valkyrienskies.core.impl.util.pollUntilEmpty
import org.valkyrienskies.physics_api.PoseVel
import org.valkyrienskies.physics_api.RigidBodyReference
import org.valkyrienskies.physics_api.SegmentTracker
import java.util.*

data class PhysShipImpl constructor(
    override val id: ShipId,
    // Don't use these outside of vs-core, I beg of thee
    val rigidBodyReference: RigidBodyReference,
    var forceInducers: List<ShipForcesInducer>,
    var _inertia: PhysInertia,

    // TODO transformation matrix
    var poseVel: PoseVel,
    var segments: SegmentTracker
) : PhysShip {
    @VSBeta
    override var buoyantFactor by rigidBodyReference::buoyantFactor

    @VSBeta
    override var doFluidDrag by rigidBodyReference::doFluidDrag

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
        invForces.pollUntilEmpty(rigidBodyReference::addInvariantForceToNextPhysTick)
        invTorques.pollUntilEmpty(rigidBodyReference::addInvariantTorqueToNextPhysTick)
        rotForces.pollUntilEmpty(rigidBodyReference::addRotDependentForceToNextPhysTick)
        rotTorques.pollUntilEmpty(rigidBodyReference::addRotDependentTorqueToNextPhysTick)

        while (invPosForces.isNotEmpty()) {
            rigidBodyReference.addInvariantForceAtPosToNextPhysTick(
                invPosPositions.removeFirst(),
                invPosForces.removeFirst()
            )
        }

        check(invPosPositions.isEmpty())
        check(invPosForces.isEmpty())

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

        invTorques.add(torque)
    }

    override fun applyRotDependentForce(force: Vector3dc) {
        requireIsFinite(force)
        assertIsPhysicsThread()

        rotForces.add(force)
    }

    override fun applyRotDependentTorque(torque: Vector3dc) {
        requireIsFinite(torque)
        assertIsPhysicsThread()

        rotTorques.add(torque)
    }

    override fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc) {
        requireIsFinite(force)
        requireIsFinite(pos)
        assertIsPhysicsThread()

        invPosForces.add(force)
        invPosPositions.add(pos)
    }

    override fun applyRotDependentForceToPos(forceInWorld: Vector3dc, relPosInWorld: Vector3dc) {
        applyRotDependentForce(poseVel.rot.transformInverse(forceInWorld, Vector3d()))
        val rotDependentTorque = poseVel.rot.transformInverse(relPosInWorld.cross(forceInWorld, Vector3d()))
        applyInvariantTorque(rotDependentTorque)
    }
}
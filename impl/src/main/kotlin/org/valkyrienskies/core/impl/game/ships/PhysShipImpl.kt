package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.impl.api.ShipForcesInducer
import org.valkyrienskies.core.impl.util.assertions.assertIsPhysicsThread
import org.valkyrienskies.core.impl.util.assertions.requireIsFinite
import org.valkyrienskies.core.impl.util.pollUntilEmpty
import org.valkyrienskies.physics_api.PhysicsBodyReference
import org.valkyrienskies.physics_api.PoseVel
import java.util.ArrayDeque

data class PhysShipImpl constructor(
    override val id: ShipId,
    // Don't use these outside of vs-core, I beg of thee
    val rigidBodyReference: PhysicsBodyReference<*>,
    var forceInducers: List<ShipForcesInducer>,
    var _inertia: PhysInertia,
    private var _poseVel: PoseVel,
    internal var lastShipTeleportId: Int,
    internal val wingManager: WingManagerImpl = WingManagerImpl(),
    override var isStatic: Boolean = false
) : PhysShip {
    var poseVel: PoseVel
        get() {
            return _poseVel
        }
        set(poseVel) {
            _poseVel = poseVel
            updatePhysTransform()
        }

    override lateinit var transform: ShipTransform
        private set

    init {
        updatePhysTransform()
    }

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

    override fun applyRotDependentForceToPos(force: Vector3dc, pos: Vector3dc) {
        requireIsFinite(force)
        requireIsFinite(pos)
        assertIsPhysicsThread()

        invPosForces.add(poseVel.rot.transform(force, Vector3d()))
        invPosPositions.add(pos)
    }

    private fun updatePhysTransform() {
        // TODO: In the future we'll need to change the scaling of this for ventities
        transform = ShipTransformImpl(
            poseVel.pos, Vector3d(rigidBodyReference.collisionShapeOffset).mul(-1.0).add(0.5, 0.5, 0.5),
            poseVel.rot, Vector3d(1.0, 1.0, 1.0)
        )
    }
}

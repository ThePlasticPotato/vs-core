package org.valkyrienskies.core.api

import org.joml.Vector3dc
import org.valkyrienskies.core.game.ships.PhysShip
import org.valkyrienskies.core.game.ships.PhysShipImpl

/**
 * Ship force inducer
 * The values shall be used every phyTick to apply forces to the ship.
 */
interface ShipForcesInducer {
    /**
     * Apply forces/torques on the physics tick
     * BE WARNED THIS GETS CALLED ON ANOTHER THREAD (the physics thread)
     *
     * @param forcesApplier Applies forces and torques to the ship
     * @param physShip The ship in the physics pipeline stage, use this for computing forces.
     *                 Please don't use [ShipData], [ShipObject] or anything else from the game stage pipeline.
     */
    fun applyForces(physShip: PhysShip)
}

/**
 * Ship torque inducer
 * The values shall be used every phyTick to apply torque to the ship.
 */
interface ForcesApplier {

    fun applyRotDependentForce(force: Vector3dc)

    fun applyInvariantForce(force: Vector3dc)

    fun applyInvariantForceToPos(force: Vector3dc, pos: Vector3dc)

    fun applyRotDependentTorque(torque: Vector3dc)

    fun applyInvariantTorque(torque: Vector3dc)

    /**
     * Sets the underlying object as a static physics object.
     *
     * @param b if true then static
     */
    fun setStatic(b: Boolean) // FIXME: ForcesAppliers are a indirect layer between rigidbodies and external use
    // setStatic is supposed to be here but it doesn't fit the name of the interface
}

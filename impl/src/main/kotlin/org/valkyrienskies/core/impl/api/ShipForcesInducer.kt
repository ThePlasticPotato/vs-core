package org.valkyrienskies.core.impl.api

import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.properties.ShipId

/**
 * Ship force inducer
 * The values shall be used every phyTick to apply forces to the ship.
 */
@Deprecated("sus")
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

    /**
     * Apply forces/torques on the physics tick, also allow looking up other ships based on the id. When looking up
     * ships based on id PLEASE only use them in a read-only manner! Modifying them will cause race conditions when we
     * eventually run these in parallel.
     */
    fun applyForcesAndLookupPhysShips(physShip: PhysShip, lookupPhysShip: (ShipId) -> PhysShip?) {
        // Default implementation to not break existing implementations
    }
}

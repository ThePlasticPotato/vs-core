package org.valkyrienskies.core.api.world

import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.physics.constraints.VSConstraint
import org.valkyrienskies.core.api.physics.constraints.VSConstraintId
import org.valkyrienskies.core.api.reference.VSRef
import java.util.concurrent.Executor

interface ValkyrienPhysicsWorld : ValkyrienBaseWorld, Executor {

    fun createBody(): PhysicsVSBody

    fun removeBody(id: BodyId)

    override fun getBody(id: BodyId): PhysicsVSBody?

    override fun getBodyReference(id: BodyId): VSRef<PhysicsVSBody>


    /**
     * @return True non-null if [vsConstraint] was created successfully.
     */
    fun createNewConstraint(vsConstraint: VSConstraint): VSConstraintId?

    /**
     * @return True iff the constraint with id [constraintId] was successfully updated.
     */
    fun updateConstraint(constraintId: VSConstraintId, updatedVSConstraint: VSConstraint): Boolean

    /**
     * @return True if a constraint with [constraintId] was removed successfully.
     */
    fun removeConstraint(constraintId: VSConstraintId): Boolean
}
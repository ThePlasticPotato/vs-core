package org.valkyrienskies.core.impl.bodies.physics.world

import org.valkyrienskies.core.api.physics.constraints.VSConstraintAndId
import org.valkyrienskies.core.impl.bodies.ServerBaseVSBodyData
import org.valkyrienskies.core.impl.bodies.VSBodyUpdateToPhysics
import org.valkyrienskies.core.impl.util.CUDQueue
import org.valkyrienskies.core.impl.util.WorldScoped
import javax.inject.Inject

// NOTE: the objects in each queue MUST be owned by this class in order to be thread-safe
@WorldScoped
class PipelineQueues @Inject constructor() {

    val bodiesToServer = CUDQueue<ServerBaseVSBodyData, ServerBaseVSBodyData>()
    val bodiesToPhysics = CUDQueue<ServerBaseVSBodyData, VSBodyUpdateToPhysics>()
    val constraintsToServer = CUDQueue<VSConstraintAndId, VSConstraintAndId>()
    val constraintsToPhysics = CUDQueue<VSConstraintAndId, VSConstraintAndId>()

}
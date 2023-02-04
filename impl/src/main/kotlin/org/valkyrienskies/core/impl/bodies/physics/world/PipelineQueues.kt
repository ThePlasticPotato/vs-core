package org.valkyrienskies.core.impl.bodies.physics.world

import org.valkyrienskies.core.api.physics.constraints.VSConstraintAndId
import org.valkyrienskies.core.impl.bodies.VSBodyCreateDataToPhysics
import org.valkyrienskies.core.impl.bodies.VSBodyCreateDataToServer
import org.valkyrienskies.core.impl.bodies.VSBodyUpdateToPhysics
import org.valkyrienskies.core.impl.bodies.VSBodyUpdateToServer
import org.valkyrienskies.core.impl.util.WorldScoped
import org.valkyrienskies.core.impl.util.cud.CUDFrameQueue
import javax.inject.Inject

// NOTE: the objects in each queue MUST be owned by this class in order to be thread-safe
@WorldScoped
class PipelineQueues @Inject constructor() {

    val bodiesToServer = CUDFrameQueue<VSBodyCreateDataToServer, VSBodyUpdateToServer>()
    val bodiesToPhysics = CUDFrameQueue<VSBodyCreateDataToPhysics, VSBodyUpdateToPhysics>()
    val constraintsToServer = CUDFrameQueue<VSConstraintAndId, VSConstraintAndId>()
    val constraintsToPhysics = CUDFrameQueue<VSConstraintAndId, VSConstraintAndId>()

}
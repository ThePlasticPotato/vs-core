package org.valkyrienskies.core.api.world

import org.valkyrienskies.core.api.bodies.PhysicsVSBody
import java.util.concurrent.Executor

interface ValkyrienPhysicsWorld : ValkyrienServerBaseWorld<PhysicsVSBody> {

    val preTickExecutor: Executor
    val dumbForceExecutor: Executor
    val smartForceExecutor: Executor
    val postTickExecutor: Executor

}

package org.valkyrienskies.core.impl.program

import org.valkyrienskies.core.apigame.VSCore
import org.valkyrienskies.core.impl.hooks.CoreHooksImpl
import org.valkyrienskies.core.impl.networking.VSNetworking
import org.valkyrienskies.core.impl.networking.VSNetworkingConfigurator
import org.valkyrienskies.core.impl.pipelines.VSPipelineComponent

/**
 * An object that lives the entirety of the program. The entrypoint for VS Core
 *
 * (well, it should be, but there are still a lot of objects that the game just reaches into directly)
 *
 * such as: [VSNetworking], [VSConfigClass]
 *
 * and the game also directly instantiates its own VSPipeline/ShipObjectClientWorld which is not great
 */
interface VSCoreInternal : VSCore {
    val networking: VSNetworking
    override val hooks: CoreHooksImpl
    val configurator: VSNetworkingConfigurator
    val pipelineComponentFactory: VSPipelineComponent.Factory
}

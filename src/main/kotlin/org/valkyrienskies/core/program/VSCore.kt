package org.valkyrienskies.core.program

import org.valkyrienskies.core.hooks.AbstractCoreHooks
import org.valkyrienskies.core.networking.VSNetworking
import org.valkyrienskies.core.networking.VSNetworkingConfigurator
import org.valkyrienskies.core.pipelines.VSPipelineComponent

/**
 * An object that lives the entirety of the program. The entrypoint for VS Core
 *
 * (well, it should be, but there are still a lot of objects that the game just reaches into directly)
 *
 * such as: [VSNetworking], [VSConfigClass]
 *
 * and the game also directly instantiates its own VSPipeline/ShipObjectClientWorld which is not great
 */
interface VSCore {
    val networking: VSNetworking
    val hooks: AbstractCoreHooks
    val configurator: VSNetworkingConfigurator
    val pipelineComponentFactory: VSPipelineComponent.Factory
}

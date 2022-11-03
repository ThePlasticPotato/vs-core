package org.valkyrienskies.core.program

import org.valkyrienskies.core.hooks.AbstractCoreHooks
import org.valkyrienskies.core.networking.NetworkChannel
import org.valkyrienskies.core.networking.VSNetworking
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule.TCP
import org.valkyrienskies.core.networking.VSNetworkingConfigurator
import org.valkyrienskies.core.pipelines.VSPipelineComponent
import javax.inject.Inject

internal class VSCoreImpl @Inject constructor(
    override val networking: VSNetworking,
    override val hooks: AbstractCoreHooks,
    override val configurator: VSNetworkingConfigurator,
    @TCP tcp: NetworkChannel,
    override val pipelineComponentFactory: VSPipelineComponent.Factory
) : VSCore {
    init {
        configurator.configure(tcp)
    }
}

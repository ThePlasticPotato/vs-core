package org.valkyrienskies.core.program

import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.config.VSCoreConfig.ServerConfigModule
import org.valkyrienskies.core.hooks.AbstractCoreHooks
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule
import org.valkyrienskies.core.networking.VSNetworkingConfigurator
import org.valkyrienskies.core.pipelines.VSPipelineComponent
import org.valkyrienskies.core.util.serialization.VSJacksonModule

@Module(
    subcomponents = [VSPipelineComponent::class],
    includes = [
        NetworkingModule::class,
        VSJacksonModule::class,
        ServerConfigModule::class
    ]
)
class VSCoreModule(
    @get:Provides val hooks: AbstractCoreHooks,
    @get:Provides val configurator: VSNetworkingConfigurator
)

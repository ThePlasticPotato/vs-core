package org.valkyrienskies.core.program

import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.hooks.AbstractCoreHooks
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule
import org.valkyrienskies.core.networking.VSNetworkingConfigurator

@Module(includes = [NetworkingModule::class])
class VSCoreModule(
    @get:Provides val hooks: AbstractCoreHooks,
    @get:Provides val configurator: VSNetworkingConfigurator
)

package org.valkyrienskies.core.program

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.api.hooks.CoreHooksOut
import org.valkyrienskies.core.api.world.chunks.BlockTypes
import org.valkyrienskies.core.config.VSCoreConfig.ServerConfigModule
import org.valkyrienskies.core.game.BlockTypesImpl
import org.valkyrienskies.core.game.ships.serialization.ShipSerializationModule
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule
import org.valkyrienskies.core.networking.VSNetworkingConfigurator
import org.valkyrienskies.core.pipelines.VSPipelineComponent
import org.valkyrienskies.core.util.serialization.VSJacksonModule

@Module(
    subcomponents = [VSPipelineComponent::class],
    includes = [
        NetworkingModule::class,
        VSJacksonModule::class,
        ServerConfigModule::class,
        ShipSerializationModule::class,
        VSCoreModule.Declarations::class
    ]
)
class VSCoreModule(
    @get:Provides val hooks: CoreHooksOut,
    @get:Provides val configurator: VSNetworkingConfigurator
) {

    @Module
    interface Declarations {
        @Binds
        fun blockTypes(impl: BlockTypesImpl): BlockTypes
    }
}

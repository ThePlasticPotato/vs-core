package org.valkyrienskies.core.impl.program

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.apigame.hooks.CoreHooksOut
import org.valkyrienskies.core.apigame.world.chunks.BlockTypes
import org.valkyrienskies.core.impl.config.VSCoreConfig.ServerConfigModule
import org.valkyrienskies.core.impl.game.BlockTypesImpl
import org.valkyrienskies.core.impl.game.ships.serialization.ShipSerializationModule
import org.valkyrienskies.core.impl.networking.VSNetworking.NetworkingModule
import org.valkyrienskies.core.impl.networking.VSNetworkingConfigurator
import org.valkyrienskies.core.impl.pipelines.VSPipelineComponent
import org.valkyrienskies.core.impl.util.serialization.VSJacksonModule

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

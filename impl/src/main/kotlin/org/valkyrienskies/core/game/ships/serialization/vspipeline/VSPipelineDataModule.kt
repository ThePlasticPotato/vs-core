package org.valkyrienskies.core.game.ships.serialization.vspipeline

import dagger.Binds
import dagger.Module
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV1Updater
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV1UpdaterImpl
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV2Updater
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV2UpdaterImpl

@Module
internal interface VSPipelineDataModule {

    @Binds
    fun converter(converter: VSPipelineDataConverterImpl): VSPipelineDataConverter

    @Binds
    fun v1(impl: VSPipelineDataV1UpdaterImpl): VSPipelineDataV1Updater

    @Binds
    fun v2(impl: VSPipelineDataV2UpdaterImpl): VSPipelineDataV2Updater
}

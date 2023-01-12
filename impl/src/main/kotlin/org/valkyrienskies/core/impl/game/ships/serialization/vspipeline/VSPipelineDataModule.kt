package org.valkyrienskies.core.impl.game.ships.serialization.vspipeline

import dagger.Binds
import dagger.Module
import org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto.VSPipelineDataV1Updater
import org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto.VSPipelineDataV1UpdaterImpl
import org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto.VSPipelineDataV2Updater
import org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto.VSPipelineDataV2UpdaterImpl
import org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto.VSPipelineDataV3Updater
import org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto.VSPipelineDataV3UpdaterImpl

@Module
interface VSPipelineDataModule {

    @Binds
    fun converter(converter: VSPipelineDataConverterImpl): VSPipelineDataConverter

    @Binds
    fun v1(impl: VSPipelineDataV1UpdaterImpl): VSPipelineDataV1Updater

    @Binds
    fun v2(impl: VSPipelineDataV2UpdaterImpl): VSPipelineDataV2Updater

    @Binds
    fun v3(impl: VSPipelineDataV3UpdaterImpl): VSPipelineDataV3Updater
}

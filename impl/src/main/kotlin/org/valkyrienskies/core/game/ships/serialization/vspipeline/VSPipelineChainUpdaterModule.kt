package org.valkyrienskies.core.game.ships.serialization.vspipeline

import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.game.ships.serialization.ChainUpdater
import org.valkyrienskies.core.game.ships.serialization.ChainUpdaterImpl
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV1
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV1Updater
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV2
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV2Updater
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV3

@Module
class VSPipelineChainUpdaterModule {

    @Provides
    fun updater(v1: VSPipelineDataV1Updater, v2: VSPipelineDataV2Updater): ChainUpdater<VSPipelineDataV3> =
        ChainUpdaterImpl(
            updateTo = VSPipelineDataV3::class.java,
            VSPipelineDataV1::class.java to v1,
            VSPipelineDataV2::class.java to v2
        )
}

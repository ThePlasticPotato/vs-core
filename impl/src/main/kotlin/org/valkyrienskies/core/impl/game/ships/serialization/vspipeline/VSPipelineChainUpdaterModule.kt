package org.valkyrienskies.core.impl.game.ships.serialization.vspipeline

import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.impl.game.ships.serialization.ChainUpdater
import org.valkyrienskies.core.impl.game.ships.serialization.ChainUpdaterImpl
import org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.dto.*

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

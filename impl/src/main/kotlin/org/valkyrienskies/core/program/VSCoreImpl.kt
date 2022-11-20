package org.valkyrienskies.core.program

import org.valkyrienskies.core.api.world.VSPipeline
import org.valkyrienskies.core.api.world.chunks.TerrainUpdate
import org.valkyrienskies.core.game.ships.SerializedShipDataModule
import org.valkyrienskies.core.game.ships.serialization.vspipeline.VSPipelineSerializer
import org.valkyrienskies.core.game.ships.types.DenseTerrainUpdateBuilderImpl
import org.valkyrienskies.core.game.ships.types.SparseTerrainUpdateBuilderImpl
import org.valkyrienskies.core.hooks.AbstractCoreHooks
import org.valkyrienskies.core.networking.NetworkChannel
import org.valkyrienskies.core.networking.VSNetworking
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule.TCP
import org.valkyrienskies.core.networking.VSNetworkingConfigurator
import org.valkyrienskies.core.pipelines.VSPipelineImpl
import org.valkyrienskies.core.pipelines.VSPipelineComponent
import javax.inject.Inject

internal class VSCoreImpl @Inject constructor(
    override val networking: VSNetworking,
    override val hooks: AbstractCoreHooks,
    override val configurator: VSNetworkingConfigurator,
    @TCP tcp: NetworkChannel,
    override val pipelineComponentFactory: VSPipelineComponent.Factory,
    private val pipelineSerializer: VSPipelineSerializer
) : VSCoreInternal {
    init {
        configurator.configure(tcp)
    }

    override fun newDenseTerrainUpdateBuilder(chunkX: Int, chunkY: Int, chunkZ: Int): TerrainUpdate.Builder {
        return DenseTerrainUpdateBuilderImpl(chunkX, chunkY, chunkZ)
    }

    override fun newSparseTerrainUpdateBuilder(chunkX: Int, chunkY: Int, chunkZ: Int): TerrainUpdate.Builder {
        return SparseTerrainUpdateBuilderImpl(chunkX, chunkY, chunkZ)
    }

    override fun newPipelineLegacyData(queryableShipDataBytes: ByteArray, chunkAllocatorBytes: ByteArray): VSPipelineImpl {
        val module = pipelineSerializer.deserializeLegacy(queryableShipDataBytes, chunkAllocatorBytes)
        return fromModule(module)
    }

    override fun newPipeline(): VSPipelineImpl {
        return fromModule(SerializedShipDataModule.createEmpty())
    }

    override fun newPipeline(data: ByteArray): VSPipelineImpl {
        return fromModule(pipelineSerializer.deserialize(data))
    }

    override fun serializePipeline(pipeline: VSPipeline): ByteArray {
        return pipelineSerializer.serialize(pipeline as VSPipelineImpl)
    }

    private fun fromModule(module: SerializedShipDataModule): VSPipelineImpl {
        return pipelineComponentFactory.newPipelineComponent(module).newPipeline()
    }
}

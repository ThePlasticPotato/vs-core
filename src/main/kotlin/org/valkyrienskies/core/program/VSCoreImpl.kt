package org.valkyrienskies.core.program

import org.valkyrienskies.core.game.ships.SerializedShipDataModule
import org.valkyrienskies.core.game.ships.serialization.vspipeline.VSPipelineSerializer
import org.valkyrienskies.core.hooks.AbstractCoreHooks
import org.valkyrienskies.core.networking.NetworkChannel
import org.valkyrienskies.core.networking.VSNetworking
import org.valkyrienskies.core.networking.VSNetworking.NetworkingModule.TCP
import org.valkyrienskies.core.networking.VSNetworkingConfigurator
import org.valkyrienskies.core.pipelines.VSPipeline
import org.valkyrienskies.core.pipelines.VSPipelineComponent
import javax.inject.Inject

internal class VSCoreImpl @Inject constructor(
    override val networking: VSNetworking,
    override val hooks: AbstractCoreHooks,
    override val configurator: VSNetworkingConfigurator,
    @TCP tcp: NetworkChannel,
    override val pipelineComponentFactory: VSPipelineComponent.Factory,
    private val pipelineSerializer: VSPipelineSerializer
) : VSCore {
    init {
        configurator.configure(tcp)
    }

    override fun newPipelineLegacyData(queryableShipDataBytes: ByteArray, chunkAllocatorBytes: ByteArray): VSPipeline {
        val module = pipelineSerializer.deserializeLegacy(queryableShipDataBytes, chunkAllocatorBytes)
        return fromModule(module)
    }

    override fun newPipeline(): VSPipeline {
        return fromModule(SerializedShipDataModule.createEmpty())
    }

    override fun newPipeline(data: ByteArray): VSPipeline {
        return fromModule(pipelineSerializer.deserialize(data))
    }

    override fun serializePipeline(pipeline: VSPipeline): ByteArray {
        return pipelineSerializer.serialize(pipeline)
    }

    private fun fromModule(module: SerializedShipDataModule): VSPipeline {
        return pipelineComponentFactory.newPipelineComponent(module).newPipeline()
    }
}

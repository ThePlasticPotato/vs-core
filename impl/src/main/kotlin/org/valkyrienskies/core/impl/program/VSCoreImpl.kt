package org.valkyrienskies.core.impl.program

import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.apigame.VSCoreCommands
import org.valkyrienskies.core.apigame.world.VSPipeline
import org.valkyrienskies.core.apigame.world.chunks.BlockTypes
import org.valkyrienskies.core.apigame.world.chunks.TerrainUpdate
import org.valkyrienskies.core.impl.game.ChunkClaimImpl
import org.valkyrienskies.core.impl.game.VSCoreCommandsImpl
import org.valkyrienskies.core.impl.game.ships.modules.ShipWorldModule
import org.valkyrienskies.core.impl.game.ships.serialization.vspipeline.VSPipelineSerializer
import org.valkyrienskies.core.impl.game.ships.types.DenseTerrainUpdateBuilderImpl
import org.valkyrienskies.core.impl.game.ships.types.SparseTerrainUpdateBuilderImpl
import org.valkyrienskies.core.impl.game.ships.types.TerrainUpdateImpl
import org.valkyrienskies.core.impl.hooks.CoreHooksImpl
import org.valkyrienskies.core.impl.networking.NetworkChannel
import org.valkyrienskies.core.impl.networking.VSNetworking
import org.valkyrienskies.core.impl.networking.VSNetworking.NetworkingModule.TCP
import org.valkyrienskies.core.impl.networking.VSNetworkingConfigurator
import org.valkyrienskies.core.impl.pipelines.VSPipelineComponent
import org.valkyrienskies.core.impl.pipelines.VSPipelineImpl
import org.valkyrienskies.physics_api.voxel_updates.DeleteVoxelShapeUpdate
import org.valkyrienskies.physics_api.voxel_updates.EmptyVoxelShapeUpdate
import org.valkyrienskies.physics_api_krunch.KrunchBootstrap
import javax.inject.Inject

class VSCoreImpl @Inject constructor(
    override val networking: VSNetworking,
    override val hooks: CoreHooksImpl,
    override val configurator: VSNetworkingConfigurator,
    @TCP tcp: NetworkChannel,
    override val pipelineComponentFactory: VSPipelineComponent.Factory,
    private val pipelineSerializer: VSPipelineSerializer,
    override val blockTypes: BlockTypes
) : VSCoreInternal, VSCoreCommands by VSCoreCommandsImpl {
    init {
        configurator.configure(tcp)
        networking.init()
    }

    override fun newEmptyVoxelShapeUpdate(chunkX: Int, chunkY: Int, chunkZ: Int, overwrite: Boolean): TerrainUpdate {
        return TerrainUpdateImpl(EmptyVoxelShapeUpdate(chunkX, chunkY, chunkZ, false, overwrite))
    }

    override fun newDeleteTerrainUpdate(chunkX: Int, chunkY: Int, chunkZ: Int): TerrainUpdate {
        return TerrainUpdateImpl(DeleteVoxelShapeUpdate(chunkX, chunkY, chunkZ))
    }

    override fun newDenseTerrainUpdateBuilder(chunkX: Int, chunkY: Int, chunkZ: Int): TerrainUpdate.Builder {
        return DenseTerrainUpdateBuilderImpl(chunkX, chunkY, chunkZ)
    }

    override fun newSparseTerrainUpdateBuilder(chunkX: Int, chunkY: Int, chunkZ: Int): TerrainUpdate.Builder {
        return SparseTerrainUpdateBuilderImpl(chunkX, chunkY, chunkZ)
    }

    override fun newPipelineLegacyData(
        queryableShipDataBytes: ByteArray, chunkAllocatorBytes: ByteArray
    ): VSPipelineImpl {
        val module = pipelineSerializer.deserializeLegacy(queryableShipDataBytes, chunkAllocatorBytes)
        return fromModule(module)
    }

    override fun newPipeline(): VSPipelineImpl {
        return fromModule(ShipWorldModule.createEmpty())
    }

    override fun newPipeline(data: ByteArray): VSPipelineImpl {
        return fromModule(pipelineSerializer.deserialize(data))
    }

    override fun serializePipeline(pipeline: VSPipeline): ByteArray {
        return pipelineSerializer.serialize(pipeline as VSPipelineImpl)
    }

    override fun newChunkClaim(claimX: Int, claimZ: Int): ChunkClaim {
        return ChunkClaimImpl(claimX, claimZ)
    }

    override fun newChunkClaimFromChunkPos(chunkX: Int, chunkZ: Int): ChunkClaim {
        return ChunkClaimImpl(ChunkClaim.getClaimXIndex(chunkX), ChunkClaim.getClaimZIndex(chunkZ))
    }

    @Deprecated("Surely we can do better than this")
    override var clientUsesUDP: Boolean by networking::clientUsesUDP

    private fun fromModule(module: ShipWorldModule): VSPipelineImpl {
        try {
            KrunchBootstrap.loadNativeBinaries()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return pipelineComponentFactory.newPipelineComponent(module).newPipeline()
    }
}

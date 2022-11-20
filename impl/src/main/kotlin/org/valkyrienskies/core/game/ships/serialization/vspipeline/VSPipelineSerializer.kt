package org.valkyrienskies.core.game.ships.serialization.vspipeline

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.valkyrienskies.core.game.ChunkAllocator
import org.valkyrienskies.core.game.ships.SerializedShipDataModule
import org.valkyrienskies.core.game.ships.serialization.ChainUpdater
import org.valkyrienskies.core.game.ships.serialization.shipserver.ServerShipDataConverter
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV0
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV0Updater
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineData
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV1
import org.valkyrienskies.core.game.ships.serialization.vspipeline.dto.VSPipelineDataV3
import org.valkyrienskies.core.pipelines.VSPipelineImpl
import org.valkyrienskies.core.util.serialization.VSJacksonUtil
import javax.inject.Inject
import javax.inject.Named

internal class VSPipelineSerializer @Inject constructor(
    @Named("dto") private val objectMapper: ObjectMapper,
    private val shipDataMapper: ServerShipDataConverter,
    private val updater: ChainUpdater<VSPipelineDataV3>,
    private val v0Updater: ServerShipDataV0Updater,
    private val pipelineDataConverter: VSPipelineDataConverter
) {

    fun deserializeLegacy(queryableShipDataBytes: ByteArray, chunkAllocatorBytes: ByteArray): SerializedShipDataModule {
        return asVsPipelineModule(deserializeLegacyAsV1(queryableShipDataBytes, chunkAllocatorBytes))
    }

    fun deserialize(bytes: ByteArray): SerializedShipDataModule {
        val data = objectMapper.readValue<VSPipelineData>(bytes)
        val updated = updater.updateToLatest(data)
        return pipelineDataConverter.convertToModel(updated)
    }

    fun serialize(pipeline: VSPipelineImpl): ByteArray {
        val ships = pipeline.shipWorld.allShips.map(shipDataMapper::convertToDto)
        val chunks = pipeline.shipWorld.chunkAllocator

        val pipelineData = VSPipelineDataV3(chunks, ships)

        return objectMapper.writeValueAsBytes(pipelineData)
    }

    private fun asVsPipelineModule(pipelineData: VSPipelineData): SerializedShipDataModule {
        return pipelineDataConverter.convertToModel(updater.updateToLatest(pipelineData))
    }

    private fun deserializeLegacyAsV1(
        queryableShipDataBytes: ByteArray, chunkAllocatorBytes: ByteArray
    ): VSPipelineDataV1 {
        val ships: List<ServerShipDataV0> = VSJacksonUtil.defaultMapper.readValue(queryableShipDataBytes)
        val allocator: ChunkAllocator = VSJacksonUtil.defaultMapper.readValue(chunkAllocatorBytes)
        val updated = ships.map { v0Updater.update(it) }

        return VSPipelineDataV1(allocator, updated)
    }
}


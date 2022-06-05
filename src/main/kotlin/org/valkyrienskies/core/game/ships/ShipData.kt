package org.valkyrienskies.core.game.ships

import com.fasterxml.jackson.annotation.JsonIgnore
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.chunk_tracking.IShipActiveChunksSet
import org.valkyrienskies.core.chunk_tracking.ShipActiveChunksSet
import org.valkyrienskies.core.game.ChunkClaim
import org.valkyrienskies.core.game.VSBlockType
import org.valkyrienskies.core.util.serialization.VSPacketIgnore
import java.util.UUID

/**
 * The purpose of [ShipData] is to keep track of the state of a ship; it does not manage the behavior of a ship.
 *
 * See [ShipObject] to find the code that defines ship behavior (movement, player interactions, etc)
 */
class ShipData(
    shipUUID: UUID,
    name: String,
    chunkClaim: ChunkClaim,
    physicsData: ShipPhysicsData,
    @VSPacketIgnore val inertiaData: ShipInertiaData,
    shipTransform: ShipTransform,
    prevTickShipTransform: ShipTransform,
    shipAABB: AABBdc,
    shipActiveChunksSet: IShipActiveChunksSet,
    var isStatic: Boolean = false
) : ShipDataCommon(
    shipUUID, name, chunkClaim, physicsData, shipTransform, prevTickShipTransform,
    shipAABB, shipActiveChunksSet
) {
    /**
     * The set of chunks that must be loaded before this ship is fully loaded.
     *
     * We need to keep track of this regardless of whether a ShipObject for this exists, so we keep track of it here.
     *
     * Also, this is transient, so we don't want to save it
     */
    @JsonIgnore
    private val missingLoadedChunks: IShipActiveChunksSet = ShipActiveChunksSet.create()

    init {
        shipActiveChunksSet.iterateChunkPos { chunkX: Int, chunkZ: Int ->
            missingLoadedChunks.addChunkPos(chunkX, chunkZ)
        }
    }

    override fun onSetBlock(
        posX: Int,
        posY: Int,
        posZ: Int,
        oldBlockType: VSBlockType,
        newBlockType: VSBlockType,
        oldBlockMass: Double,
        newBlockMass: Double
    ) {
        super.onSetBlock(posX, posY, posZ, oldBlockType, newBlockType, oldBlockMass, newBlockMass)

        // Update [inertiaData]
        inertiaData.onSetBlock(posX, posY, posZ, oldBlockMass, newBlockMass)
    }

    fun onLoadChunk(chunkX: Int, chunkZ: Int) {
        if (chunkClaim.contains(chunkX, chunkZ)) {
            missingLoadedChunks.removeChunkPos(chunkX, chunkZ)
        }
    }

    fun onUnloadChunk(chunkX: Int, chunkZ: Int) {
        if (chunkClaim.contains(chunkX, chunkZ) && shipActiveChunksSet.containsChunkPos(chunkX, chunkZ)) {
            missingLoadedChunks.addChunkPos(chunkX, chunkZ)
        }
    }

    fun areVoxelsFullyLoaded(): Boolean {
        // We are fully loaded if we have 0 missing chunks
        return missingLoadedChunks.getTotalChunks() == 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ShipData

        if (inertiaData != other.inertiaData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + inertiaData.hashCode()
        return result
    }

    companion object {
        /**
         * Creates a new [ShipData] from the given name and coordinates. The resulting [ShipData] is completely empty,
         * so it must be filled with blocks by other code.
         */
        internal fun createEmpty(
            name: String,
            chunkClaim: ChunkClaim,
            shipCenterInWorldCoordinates: Vector3dc,
            shipCenterInShipCoordinates: Vector3dc,
            scaling: Double = 1.0,
            isStatic: Boolean = false
        ): ShipData {
            val shipTransform = ShipTransform.createFromCoordinatesAndRotationAndScaling(
                shipCenterInWorldCoordinates,
                shipCenterInShipCoordinates,
                Quaterniond().fromAxisAngleDeg(0.0, 1.0, 0.0, 0.0),
                Vector3d(scaling)
            )

            return ShipData(
                shipUUID = UUID.randomUUID(),
                name = name,
                chunkClaim = chunkClaim,
                physicsData = ShipPhysicsData.createEmpty(),
                inertiaData = ShipInertiaData.newEmptyShipInertiaData(),
                shipTransform = shipTransform,
                prevTickShipTransform = shipTransform,
                shipAABB = AABBd(),
                shipActiveChunksSet = ShipActiveChunksSet.create(),
                isStatic = isStatic
            )
        }
    }
}

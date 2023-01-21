package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.attachment.AttachmentSerializationStrategy
import org.valkyrienskies.core.api.bodies.shape.VoxelUpdate

interface VSCoreApi {
    fun newEmptyVoxelUpdate(chunkX: Int, chunkY: Int, chunkZ: Int, overwrite: Boolean): VoxelUpdate

    /**
     * Creates a new voxel update that deletes the specified chunk
     */
    fun newDeleteVoxelUpdate(chunkX: Int, chunkY: Int, chunkZ: Int): VoxelUpdate

    /**
     * Creates a new dense voxel update builder. A dense voxel update will
     * update every single block in the chunk, and by default contains only air.
     */
    fun newDenseVoxelUpdateBuilder(chunkX: Int, chunkY: Int, chunkZ: Int): VoxelUpdate.Builder

    /**
     * Creates a new sparse voxel update builder. A sparse voxel update will
     * only update blocks that are added to it.
     */
    fun newSparseVoxelUpdateBuilder(chunkX: Int, chunkY: Int, chunkZ: Int): VoxelUpdate.Builder

    fun <T> registerAttachmentSerializationStrategy(name: String, strategy: Class<out AttachmentSerializationStrategy>)

    fun registerAttachments(vararg classes: Class<*>)

    fun registerAttachmentsInPackage(packageName: String)

}
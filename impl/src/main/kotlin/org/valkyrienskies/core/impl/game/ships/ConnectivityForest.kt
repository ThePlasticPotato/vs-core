package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3ic
import org.valkyrienskies.core.impl.datastructures.BlockPos2ObjectOpenHashMap
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex

interface ConnectivityForest {

    val graph: ConnGraph

    val vertices: BlockPos2ObjectOpenHashMap<BlockPosVertex>

    /**
     * Attempts to add a new block to the connectivity graph.
     * Returns true if the block was added, false if it already existed in the graph.
     */
    fun newVertex(posX: Int, posY: Int, posZ: Int): Boolean

    /**
     * Attempts to remove a block from the connectivity graph.
     * Returns true if the block was removed, false if it didn't exist in the graph.
     */
    fun delVertex(posX: Int, posY: Int, posZ: Int): Boolean

    /**
     * Attempts to split the forest in two, with one being smaller or equal in scale and being applied to a new ship.
     * Returns true if it succeeds, false otherwise.
     */
    fun split(vectorOne: Vector3ic, vectorTwo: Vector3ic): Boolean

    // todo: later
    fun merge()
}

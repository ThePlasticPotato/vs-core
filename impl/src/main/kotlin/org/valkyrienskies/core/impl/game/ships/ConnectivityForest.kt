package org.valkyrienskies.core.impl.game.ships

import org.valkyrienskies.core.impl.datastructures.BlockPos2ObjectOpenHashMap
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex

interface ConnectivityForest {

    val graph: ConnGraph

    val vertices: BlockPos2ObjectOpenHashMap<ConnVertex>

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
    fun split(posX: Int, posY: Int, posZ: Int, posX2: Int, posY2: Int, posZ2: Int): Boolean

    // todo: later
    fun merge()
}

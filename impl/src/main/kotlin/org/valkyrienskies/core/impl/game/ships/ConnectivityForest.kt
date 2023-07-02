package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3ic
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import java.util.concurrent.CopyOnWriteArraySet

interface ConnectivityForest {

    /**
     * The dynamic connectivity graph itself.
     */
    val graph: ConnGraph

    /**
     * A hashmap of all the vertices in the graph.
     */

    val vertices: HashMap<Vector3ic, BlockPosVertex>

    /**
     * A queue for breakages to be sent to VS2 itself.
     */

    val breakages: MutableSet<ArrayList<Vector3ic?>>

    val breakagesToAdd: MutableSet<ArrayList<Vector3ic?>>
    val breakagesToRemove: MutableSet<ArrayList<Vector3ic?>>

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
     * Scans a ship, and returns the smaller, split half of the graph.
     */
    fun split(vertlist: ArrayList<Vector3ic?>): Set<Pair<HashMap<Vector3ic, BlockPosVertex>, Vector3ic>>

    // todo: later
    fun merge()

    fun addToBreakQueue(arr : ArrayList<Vector3ic?>)

    fun removeFromBreakQueue(arr : ArrayList<Vector3ic?>)

    fun getBreakQueue() : Set<ArrayList<Vector3ic?>>

    fun gameTick()
}

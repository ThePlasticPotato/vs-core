package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3ic
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph

interface AirPocketForest {

    /**
     * The graph of air blocks.
     */
    val graph: ConnGraph

    /**
     * A hashmap of all the vertices in the graph.
     */
    val airVertices: HashMap<Vector3ic, BlockPosVertex>

    /**
     * A hashmap of all the vertices considered to be "outside" of any air pockets.
     */
    public val outsideAirVertices: HashMap<Vector3ic, BlockPosVertex>

    /**
     * A set of all the air blocks that are split from outside air.
     */
    val sealedAirBlocks: HashMap<Vector3ic, BlockPosVertex>

    /**
     * A set of each individual air pocket as a set.
     */
    val individualAirPockets: HashMap<Int, HashMap<Vector3ic, BlockPosVertex>>

    /**
     * A boolean that is set to true if the outside air vertices need to be updated. For VS2 communication.
     */
    var shouldUpdateOutsideAir: Boolean

    /**
     * The current AABB of the ship.
     */
    var currentShipAABB: AABBic

    /**
     * The ID of the ship this is attached to. Not exactly sure I should be storing this...
     */
    val hostShipId: ShipId

    /**
     * Returns whether the given position is within one of the air pockets listed in airPockets.
     */
    fun isInAirPocket(posX: Int, posY: Int, posZ: Int): Boolean

    /**
     * Checks that the listed breakpoints are, infact, still connected; if not, creates an air pocket out of the set within the newly sealed room
     */
    fun verifyAirPocket(changed: Vector3ic) : Boolean

    /**
     * Attempts to add a new block to the connectivity graph.
     * Returns true if the block was added, false if it already existed in the graph.
     */
    fun newVertex(posX: Int, posY: Int, posZ: Int, silent: Boolean): Boolean

    /**
     * Attempts to remove a block from the connectivity graph.
     * Returns true if the block was removed, false if it didn't exist in the graph.
     */
    fun delVertex(posX: Int, posY: Int, posZ: Int, silent: Boolean): Boolean

    /**
     * Refreshes the list of the outermost air vertices, which will always be outside.
     */
    fun updateOutsideAirVertices(new: Set<Vector3ic>)

    fun setUpdateOutsideAir(bool: Boolean)
    fun toUpdateOutsideAir(): Boolean
}

package org.valkyrienskies.core.api.ships

import org.joml.Matrix4dc

typealias WingGroupId = Int

interface WingManager {

    fun createWingGroup(): WingGroupId

    fun deleteWingGroup(wingGroupId: WingGroupId): Boolean

    /**
     * Returns true if this replaced an old wing
     */
    fun setWing(wingGroupId: WingGroupId, posX: Int, posY: Int, posZ: Int, wing: Wing?): Boolean

    fun getWing(wingGroupId: WingGroupId, posX: Int, posY: Int, posZ: Int): Wing?

    fun setWingGroupTransform(wingGroupId: WingGroupId, transform: Matrix4dc)

    /**
     * Get the changes that occurred since the last tick. Used by the game pipeline to send wings to the physics
     * pipeline
     */
    fun getWingChanges(): WingManagerChanges?

    /**
     * Clear the changes stored by this object. Should only be used by vs-core.
     */
    fun clearWingChanges()

    // Used by VS2 to get the wing group to put world wings in
    fun getFirstWingGroupId(): WingGroupId
}

data class WingGroupChanges(
    val changedWings: List<PositionedWing>?,
    val changedTransform: Matrix4dc?
)

data class WingManagerChanges(
    val wingGroupChanges: List<Pair<WingGroupId, WingGroupChanges>>?,
    val deletedWingGroups: List<WingGroupId>?
)

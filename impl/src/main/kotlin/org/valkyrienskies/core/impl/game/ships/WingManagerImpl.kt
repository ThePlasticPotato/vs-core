package org.valkyrienskies.core.impl.game.ships

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import org.joml.Matrix4d
import org.joml.Matrix4dc
import org.valkyrienskies.core.api.ships.PositionedWing
import org.valkyrienskies.core.api.ships.Wing
import org.valkyrienskies.core.api.ships.WingGroupChanges
import org.valkyrienskies.core.api.ships.WingGroupId
import org.valkyrienskies.core.api.ships.WingManager
import org.valkyrienskies.core.api.ships.WingManagerChanges
import org.valkyrienskies.core.impl.datastructures.BlockPos2ObjectOpenHashMap

class WingManagerImpl : WingManager {

    private val wingGroups: Int2ObjectMap<WingGroupImpl> = Int2ObjectOpenHashMap()
    private val wingGroupsDeletedThisTick: IntList = IntArrayList()
    private var nextWingGroupId = 0

    override fun createWingGroup(): WingGroupId {
        val newWingGroupId = nextWingGroupId++
        wingGroups[newWingGroupId] = WingGroupImpl()
        return newWingGroupId
    }

    override fun deleteWingGroup(wingGroupId: WingGroupId): Boolean {
        val deleteSuccess = (wingGroups.remove(wingGroupId) != null)
        if (deleteSuccess) {
            wingGroupsDeletedThisTick.add(wingGroupId)
        }
        return deleteSuccess
    }

    override fun setWing(wingGroupId: WingGroupId, posX: Int, posY: Int, posZ: Int, wing: Wing?): Boolean {
        return wingGroups[wingGroupId]!!.setWing(posX, posY, posZ, wing)
    }

    override fun getWing(wingGroupId: WingGroupId, posX: Int, posY: Int, posZ: Int): Wing? {
        return wingGroups[wingGroupId]!!.getWing(posX, posY, posZ)
    }

    override fun setWingGroupTransform(wingGroupId: WingGroupId, transform: Matrix4dc) {
        wingGroups[wingGroupId]!!.setTransform(transform)
    }

    override fun getWingChanges(): WingManagerChanges? {
        var wingGroupChangesList: MutableList<Pair<WingGroupId, WingGroupChanges>>? = null
        for (entry in wingGroups.int2ObjectEntrySet()) {
            val wingGroupId = entry.intKey
            val wingGroup = entry.value
            val wingGroupChanges = wingGroup.getWingGroupChanges()
            if (wingGroupChanges != null) {
                if (wingGroupChangesList == null) wingGroupChangesList = ArrayList()
                wingGroupChangesList.add(Pair(wingGroupId, wingGroupChanges))
            }
        }
        return if (wingGroupChangesList == null && wingGroupsDeletedThisTick.isEmpty())
            null
        else
            WingManagerChanges(wingGroupChangesList, IntArrayList(wingGroupsDeletedThisTick))
    }

    override fun clearWingChanges() {
        for (wingGroup in wingGroups.values) {
            wingGroup.clearChanges()
        }
        wingGroupsDeletedThisTick.clear()
    }

    override fun getFirstWingGroupId(): WingGroupId {
        if (nextWingGroupId == 0) {
            throw IllegalStateException("This wing manager has no wing groups!")
        }
        return 0
    }

    fun applyChanges(changes: WingManagerChanges) {
        changes.deletedWingGroups?.forEach { it ->
            wingGroups.remove(it)
        }
        changes.wingGroupChanges?.forEach { it ->
            wingGroups.getOrPut(it.first) { WingGroupImpl() }.applyChanges(it.second)
        }
    }

    internal inline fun forEachWing(
        function: (wingTransform: Matrix4dc, posX: Int, posY: Int, posZ: Int, wing: Wing) -> Unit
    ) {
        for (wingGroup in wingGroups.values) {
            wingGroup.forEachWing { posX, posY, posZ, wing ->
                function(wingGroup.wingTransform, posX, posY, posZ, wing)
            }
        }
    }
}

internal class WingGroupImpl {

    private val wingsMap: BlockPos2ObjectOpenHashMap<Wing> = BlockPos2ObjectOpenHashMap()
    private var wingChanges: MutableList<PositionedWing>? = null
    var wingTransform: Matrix4dc = Matrix4d()
        private set
    private var transformChanged: Boolean = true

    fun setWing(posX: Int, posY: Int, posZ: Int, wing: Wing?, recordChange: Boolean = true): Boolean {
        if (recordChange) {
            if (wingChanges == null) {
                wingChanges = ArrayList()
            }
            wingChanges!!.add(PositionedWing(posX, posY, posZ, wing))
        }
        return if (wing != null) {
            wingsMap.put(posX, posY, posZ, wing) != null
        } else {
            wingsMap.remove(posX, posY, posZ) != null
        }
    }

    fun getWing(posX: Int, posY: Int, posZ: Int): Wing? {
        return wingsMap.get(posX, posY, posZ)
    }

    fun getWingGroupChanges(): WingGroupChanges? {
        if (wingChanges == null && !transformChanged) return null
        return WingGroupChanges(wingChanges, if (transformChanged) wingTransform else null)
    }

    fun clearChanges() {
        wingChanges = null
        transformChanged = false
    }

    fun setTransform(transform: Matrix4dc) {
        this.wingTransform = transform
        transformChanged = true
    }

    fun applyChanges(changes: WingGroupChanges) {
        if (changes.changedTransform != null) {
            wingTransform = changes.changedTransform!!
        }
        changes.changedWings?.forEach { it ->
            setWing(it.posX, it.posY, it.posZ, it.wing, false)
        }
    }

    internal inline fun forEachWing(function: (posX: Int, posY: Int, posZ: Int, wing: Wing) -> Unit) {
        wingsMap.forEach(function)
    }
}

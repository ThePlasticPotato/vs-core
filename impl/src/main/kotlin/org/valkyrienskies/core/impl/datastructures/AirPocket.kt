package org.valkyrienskies.core.impl.datastructures

import org.joml.Vector3fc
import org.joml.Vector3i
import org.joml.Vector3ic
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import java.util.BitSet
import kotlin.math.roundToInt

class AirPocket(val id: Int, val pocket: HashMap<Vector3ic, BlockPosVertex>, val extraData: HashMap<String, Double>) {
    fun containsPoint(vec: Vector3ic): Boolean {
        return pocket.containsKey(vec)
    }
    fun containsPoint(vec: Vector3fc): Boolean {
        return pocket.containsKey(Vector3i(vec.x().roundToInt(), vec.y().roundToInt(), vec.z().roundToInt()))
    }
    fun containsPoint(x: Int, y: Int, z: Int): Boolean {
        return pocket.containsKey(Vector3i(x, y, z))
    }
    fun grabExtraData(key: String): Double? {
        return this.extraData[key]
    }
    fun writeExtraData(key: String, value: Double) {
        this.extraData[key] = value
    }
    fun writeAllExtraData(map: HashMap<String, Double>) {
        this.extraData.putAll(map)
    }
    fun addToPocket(map: HashMap<Vector3ic, BlockPosVertex>) {
        this.pocket.putAll(map)
    }
}

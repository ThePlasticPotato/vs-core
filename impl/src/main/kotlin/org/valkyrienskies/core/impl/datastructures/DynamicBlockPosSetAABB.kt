package org.valkyrienskies.core.impl.datastructures

import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap
import org.joml.primitives.AABBi

class DynamicBlockPosSetAABB @JvmOverloads constructor(private val backing: IBlockPosSet = DenseBlockPosSet()) :
    IBlockPosSet by backing, IBlockPosSetAABB {

    private val xMap = Int2IntAVLTreeMap()
    private val yMap = Int2IntAVLTreeMap()
    private val zMap = Int2IntAVLTreeMap()

    override fun add(x: Int, y: Int, z: Int): Boolean {
        if (backing.add(x, y, z)) {
            xMap.addTo(x, 1)
            yMap.addTo(y, 1)
            zMap.addTo(z, 1)

            return true
        }

        return false
    }

    override fun remove(x: Int, y: Int, z: Int): Boolean {
        if (backing.remove(x, y, z)) {
            if (xMap.addTo(x, -1) == 1) {
                xMap.remove(x)
            }
            if (yMap.addTo(y, -1) == 1) {
                yMap.remove(y)
            }
            if (zMap.addTo(z, -1) == 1) {
                zMap.remove(z)
            }

            return true
        }

        return false
    }

    override fun clear() {
        xMap.clear()
        yMap.clear()
        zMap.clear()
        backing.clear()
    }

    override fun makeAABB(): AABBi? {
        if (isEmpty()) return null

        return AABBi(
            xMap.firstIntKey(),
            yMap.firstIntKey(),
            zMap.firstIntKey(),
            xMap.lastIntKey(),
            yMap.lastIntKey(),
            zMap.lastIntKey()
        )
    }
}

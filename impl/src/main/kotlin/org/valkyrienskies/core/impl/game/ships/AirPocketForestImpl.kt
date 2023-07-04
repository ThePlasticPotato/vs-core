package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3i
import org.joml.Vector3ic
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex

class AirPocketForestImpl(
    override val graph: ConnGraph, override val airVertices: HashMap<Vector3ic, BlockPosVertex>,
    public override val outsideAirVertices: HashMap<Vector3ic, BlockPosVertex>,
    override val airPockets: HashMap<Vector3ic, BlockPosVertex>
) : AirPocketForest {

    override var shouldUpdateOutsideAir: Boolean = false
    override fun isInAirPocket(posX: Int, posY: Int, posZ: Int): Boolean {
        for (vertex in airPockets.values) {
            if (vertex.posX == posX && vertex.posY == posY && vertex.posZ == posZ) {
                    return true
            }
        }
        return false
    }

    override fun verifyAirPocket(changed: BlockPosVertex): Boolean {
        var isOutside = false
        for (vertex in outsideAirVertices.values) {
            if (graph.connected(changed, vertex)) {
                isOutside = true
                break
            }
        }
        return if (!isOutside) {
            val set: MutableSet<BlockPosVertex> = mutableSetOf()
            set.add(changed)
            for (vertex in airVertices.values) {
                if (graph.connected(changed, vertex)) {
                    set.add(vertex)
                }
            }
            addAirPocket(set)
            true
        } else {
            removeAirPocket(changed)
            false
        }
    }

    fun addAirPocket(toAdd: Set<BlockPosVertex>) {
        for (vertex in toAdd) {
            if (airPockets.values.contains(vertex)) {
                continue
            } else {
                airPockets.put(Vector3i(vertex.posX, vertex.posY, vertex.posZ), vertex)
            }
        }
    }

    fun removeAirPocket(blockPosVertex: BlockPosVertex) {
        if (isInAirPocket(blockPosVertex.posX, blockPosVertex.posY, blockPosVertex.posZ)) {
            for (vertex in airPockets.values) {
                if (vertex.posX == blockPosVertex.posX && vertex.posY == blockPosVertex.posY && vertex.posZ == blockPosVertex.posZ) {
                    airPockets.remove(Vector3i(vertex.posX, vertex.posY, vertex.posZ))
                    return
                }
            }
        }
    }

    override fun newVertex(posX: Int, posY: Int, posZ: Int): Boolean {
        if (airVertices.get(Vector3i(posX, posY, posZ)) != null) return false

        val vertex = BlockPosVertex(posX, posY, posZ)

        airVertices.put(Vector3i(posX, posY, posZ), vertex)

        if (airVertices.contains(Vector3i(posX + 1, posY, posZ))) {
            graph.addEdge(vertex, airVertices[Vector3i(posX + 1, posY, posZ)]!!)
        }
        if (airVertices.contains(Vector3i(posX - 1, posY, posZ))) {
            graph.addEdge(vertex, airVertices[Vector3i(posX - 1, posY, posZ)]!!)
        }
        if (airVertices.contains(Vector3i(posX, posY + 1, posZ))) {
            graph.addEdge(vertex, airVertices[Vector3i(posX, posY + 1, posZ)]!!)
        }
        if (airVertices.contains(Vector3i(posX, posY - 1, posZ))) {
            graph.addEdge(vertex, airVertices[Vector3i(posX, posY - 1, posZ)]!!)
        }
        if (airVertices.contains(Vector3i(posX, posY, posZ + 1))) {
            graph.addEdge(vertex, airVertices[Vector3i(posX, posY, posZ + 1)]!!)
        }
        if (airVertices.contains(Vector3i(posX, posY, posZ - 1))) {
            graph.addEdge(vertex, airVertices[Vector3i(posX, posY, posZ - 1)]!!)
        }

        verifyAirPocket(vertex)

        return true
    }

    override fun delVertex(posX: Int, posY: Int, posZ: Int): Boolean {
        if (airVertices.get(Vector3i(posX, posY, posZ)) == null) return false

        val vertex: BlockPosVertex = airVertices[Vector3i(posX, posY, posZ)]!!

        val list : Collection<BlockPosVertex> = graph.adjacentVertices(vertex) as Collection<BlockPosVertex>

        if (list.isEmpty()) {
            airVertices.remove(Vector3i(posX, posY, posZ))
            return true
        }
        for (cVertex: BlockPosVertex in list) {
            graph.removeEdge(vertex, cVertex)
            verifyAirPocket(cVertex)
        }
        airVertices.remove(Vector3i(posX, posY, posZ))

        return true
    }

    override fun updateOutsideAirVertices(new: Set<BlockPosVertex>) {
        outsideAirVertices.clear()
        for (vertex in new) {
            outsideAirVertices.put(Vector3i(vertex.posX, vertex.posY, vertex.posZ), vertex)
        }
    }

    override fun setUpdateOutsideAir(bool: Boolean) {
        shouldUpdateOutsideAir = bool
    }

    override fun toUpdateOutsideAir(): Boolean {
        return shouldUpdateOutsideAir
    }
}

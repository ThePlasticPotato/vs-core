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

    override fun verifyAirPocket(changed: Vector3ic): Boolean {
        if (airVertices[changed] == null) return false
        var isOutside = false
        for (vertex in outsideAirVertices.values) {
            if (graph.connected(airVertices[changed], vertex)) {
                isOutside = true
                break
            }
        }
        val set: MutableSet<Vector3ic> = mutableSetOf()
        set.add(changed)
        for (vertex in airVertices.values) {
            if (graph.connected(airVertices[changed], vertex)) {
                set.add(Vector3i(vertex.posX, vertex.posY, vertex.posZ))
            }
        }
        return if (!isOutside) {
            addAirPocket(set)
            true
        } else {
            removeAirPocket(set)
            false
        }
    }

    fun addAirPocket(toAdd: Set<Vector3ic>) {
        for (vertex in toAdd) {
            if (airVertices[vertex] == null) {
                continue
            }
            var shouldAdd = true
            for (airPocket in airPockets.values) {
                if (airPocket.posX == vertex.x() && airPocket.posY == vertex.x() && airPocket.posZ == vertex.x()) {
                    shouldAdd = false
                    break
                }
            }
            if (shouldAdd) {
                airPockets.put(Vector3i(vertex.x(), vertex.x(), vertex.x()), airVertices[vertex]!!)
            }
        }
    }

    fun removeAirPocket(toRemove: Set<Vector3ic>) {
        for (blockPosVertex in toRemove) {
            if (isInAirPocket(blockPosVertex.x(), blockPosVertex.y(), blockPosVertex.z())) {
                for (vertex in airPockets.values) {
                    if (vertex.posX == blockPosVertex.x() && vertex.posY == blockPosVertex.y() && vertex.posZ == blockPosVertex.z()) {
                        airPockets.remove(blockPosVertex)
                        return
                    }
                }
            }
        }
    }

    override fun newVertex(posX: Int, posY: Int, posZ: Int, silent: Boolean): Boolean {
        if (airVertices[Vector3i(posX, posY, posZ)] != null) return false

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
        if (!silent) {
            verifyAirPocket(Vector3i(posX, posY, posZ))
        }

        return true
    }

    override fun delVertex(posX: Int, posY: Int, posZ: Int, silent: Boolean): Boolean {
        if (airVertices.get(Vector3i(posX, posY, posZ)) == null) return false

        val vertex: BlockPosVertex = airVertices[Vector3i(posX, posY, posZ)]!!

        val list : Collection<ConnVertex> = graph.adjacentVertices(vertex)

        if (list.isEmpty()) {
            airVertices.remove(Vector3i(posX, posY, posZ))
            return true
        }
        for (cVertex: ConnVertex in list) {
            graph.removeEdge(vertex, cVertex)
            if (cVertex is BlockPosVertex) {
                if (!silent) {
                    verifyAirPocket(Vector3i(cVertex.posX, cVertex.posY, cVertex.posZ))
                }
            }
        }
        airVertices.remove(Vector3i(posX, posY, posZ))

        return true
    }

    override fun updateOutsideAirVertices(new: Set<Vector3ic>) {
        if (new.isNotEmpty()) {
            outsideAirVertices.clear()
            for (vertex in new) {
                outsideAirVertices.put(vertex, airVertices[vertex]!!)
            }
        }
    }

    override fun setUpdateOutsideAir(bool: Boolean) {
        shouldUpdateOutsideAir = bool
    }

    override fun toUpdateOutsideAir(): Boolean {
        return shouldUpdateOutsideAir
    }
}

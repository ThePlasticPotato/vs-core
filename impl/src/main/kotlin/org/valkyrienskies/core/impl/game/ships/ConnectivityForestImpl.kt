package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3dc
import org.joml.Vector3ic
import org.valkyrienskies.core.impl.datastructures.BlockPos2ObjectOpenHashMap
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex

class ConnectivityForestImpl(override val graph: ConnGraph,
    override val vertices: BlockPos2ObjectOpenHashMap<BlockPosVertex>
) : ConnectivityForest {

    override fun newVertex(posX: Int, posY: Int, posZ: Int): Boolean {
        if (vertices.get(posX, posY, posZ) != null) return false

        val vertex = BlockPosVertex(posX, posY, posZ)
        vertices.put(posX, posY, posZ, vertex)

        if (vertices.contains(posX + 1, posY, posZ)) {
            graph.addEdge(vertex, vertices.get(posX + 1, posY, posZ)!!)
        }
        if (vertices.contains(posX - 1, posY, posZ)) {
            graph.addEdge(vertex, vertices.get(posX - 1, posY, posZ)!!)
        }
        if (vertices.contains(posX, posY + 1, posZ)) {
            graph.addEdge(vertex, vertices.get(posX, posY + 1, posZ)!!)
        }
        if (vertices.contains(posX, posY - 1, posZ)) {
            graph.addEdge(vertex, vertices.get(posX, posY - 1, posZ)!!)
        }
        if (vertices.contains(posX, posY, posZ + 1)) {
            graph.addEdge(vertex, vertices.get(posX, posY, posZ + 1)!!)
        }
        if (vertices.contains(posX, posY, posZ - 1)) {
            graph.addEdge(vertex, vertices.get(posX, posY, posZ - 1)!!)
        }

        return true
    }

    override fun delVertex(posX: Int, posY: Int, posZ: Int): Boolean {
        if (vertices.get(posX, posY, posZ) == null) return false

        val vertex: BlockPosVertex = vertices.get(posX, posY, posZ)!!

        val list : Collection<ConnVertex> = graph.adjacentVertices(vertex)

        if (list.isEmpty()) {
            vertices.remove(posX, posY, posZ)
            return true
        }
        for (cVertex: ConnVertex in list) {
            graph.removeEdge(vertex, cVertex)
        }
        vertices.remove(posX, posY, posZ)
        return true
    }

    override fun split(vectorOne: Vector3ic, vectorTwo: Vector3ic): Boolean {
        return false
    }

    override fun merge() {
        TODO("Not yet implemented")
    }
}

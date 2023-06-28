package org.valkyrienskies.core.impl.game.ships

import org.valkyrienskies.core.impl.datastructures.BlockPos2ObjectOpenHashMap
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex

class ConnectivityForestImpl(override val graph: ConnGraph,
    override val vertices: BlockPos2ObjectOpenHashMap<ConnVertex>
) : ConnectivityForest {

    override fun newVertex(posX: Int, posY: Int, posZ: Int): Boolean {
        if (vertices.get(posX, posY, posZ) != null) return false

        val vertex = ConnVertex()
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

        val vertex: ConnVertex = vertices.get(posX, posY, posZ)!!

        var list : Collection<ConnVertex> = graph.adjacentVertices(vertex)

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

    override fun split(posX: Int, posY: Int, posZ: Int, posX2: Int, posY2: Int, posZ2: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun merge() {
        TODO("Not yet implemented")
    }
}

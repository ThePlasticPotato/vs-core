package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3i
import org.joml.Vector3ic
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex

class ConnectivityForestImpl(override val graph: ConnGraph,
    override val vertices: HashMap<Vector3ic, BlockPosVertex>,
    override val breakages: MutableSet<Pair<Vector3ic, Vector3ic>>
) : ConnectivityForest {

    override fun newVertex(posX: Int, posY: Int, posZ: Int): Boolean {
        if (vertices.get(Vector3i(posX, posY, posZ)) != null) return false

        val vertex = BlockPosVertex(posX, posY, posZ)

        vertices.put(Vector3i(posX, posY, posZ), vertex)

        if (vertices.contains(Vector3i(posX + 1, posY, posZ))) {
            graph.addEdge(vertex, vertices[Vector3i(posX + 1, posY, posZ)]!!)
        }
        if (vertices.contains(Vector3i(posX - 1, posY, posZ))) {
            graph.addEdge(vertex, vertices[Vector3i(posX - 1, posY, posZ)]!!)
        }
        if (vertices.contains(Vector3i(posX, posY + 1, posZ))) {
            graph.addEdge(vertex, vertices[Vector3i(posX, posY + 1, posZ)]!!)
        }
        if (vertices.contains(Vector3i(posX, posY - 1, posZ))) {
            graph.addEdge(vertex, vertices[Vector3i(posX, posY - 1, posZ)]!!)
        }
        if (vertices.contains(Vector3i(posX, posY, posZ + 1))) {
            graph.addEdge(vertex, vertices[Vector3i(posX, posY, posZ + 1)]!!)
        }
        if (vertices.contains(Vector3i(posX, posY, posZ - 1))) {
            graph.addEdge(vertex, vertices[Vector3i(posX, posY, posZ - 1)]!!)
        }

        return true
    }

    override fun delVertex(posX: Int, posY: Int, posZ: Int): Boolean {
        if (vertices.get(Vector3i(posX, posY, posZ)) == null) return false

        val vertex: BlockPosVertex = vertices[Vector3i(posX, posY, posZ)]!!

        val list : Collection<ConnVertex> = graph.adjacentVertices(vertex)

        if (list.isEmpty()) {
            vertices.remove(Vector3i(posX, posY, posZ))
            return true
        }
        for (cVertex: ConnVertex in list) {
            graph.removeEdge(vertex, cVertex)
        }
        vertices.remove(Vector3i(posX, posY, posZ))
        return true
    }

    override fun split(vectorOne: Vector3ic, vectorTwo: Vector3ic): Pair<HashMap<Vector3ic, BlockPosVertex>, Boolean> {
        val vertexOne = vertices[Vector3i(vectorOne.x(), vectorOne.y(), vectorOne.z())]
        val vertexTwo = vertices[Vector3i(vectorTwo.x(), vectorTwo.y(), vectorTwo.z())]

        val connectedToOne = HashMap<Vector3ic, BlockPosVertex>()
        val connectedToTwo = HashMap<Vector3ic, BlockPosVertex>()

        if (vertexOne != null && vertexTwo != null) {
            vertices.forEach{vec, vertex ->
                if (graph.connected(vertexOne, vertex)) {
                    connectedToOne[vec] = vertex
                }
                if (graph.connected(vertexTwo, vertex)) {
                    connectedToTwo[vec] = vertex
                }
            }
        }

        if (connectedToOne.values.size > connectedToTwo.values.size) {
            return Pair(connectedToTwo, true)
        }
        return Pair(connectedToOne, false)
    }

    override fun merge() {
        TODO("Not yet implemented")
    }
}

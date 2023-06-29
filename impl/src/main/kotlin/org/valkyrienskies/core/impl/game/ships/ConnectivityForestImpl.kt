package org.valkyrienskies.core.impl.game.ships

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.joml.Vector3dc
import org.joml.Vector3ic
import org.valkyrienskies.core.impl.datastructures.BlockPos2ObjectOpenHashMap
import org.valkyrienskies.core.impl.datastructures.DenseBlockPosSet
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex
import java.util.Queue

class ConnectivityForestImpl(override val graph: ConnGraph,
    override val vertices: BlockPos2ObjectOpenHashMap<BlockPosVertex>,
    override val breakages: MutableSet<Pair<Vector3ic, Vector3ic>>
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

    override fun split(vectorOne: Vector3ic, vectorTwo: Vector3ic): Pair<BlockPos2ObjectOpenHashMap<BlockPosVertex>, Boolean> {
        val vertexOne = vertices.get(vectorOne.x(), vectorOne.y(), vectorOne.z())
        val vertexTwo = vertices.get(vectorTwo.x(), vectorTwo.y(), vectorTwo.z())

        val connectedToOne = BlockPos2ObjectOpenHashMap<BlockPosVertex>()
        val connectedToTwo = BlockPos2ObjectOpenHashMap<BlockPosVertex>()

        if (vertexOne != null && vertexTwo != null) {
            vertices.forEach{x, y, z, vertex -> Unit
                if (graph.connected(vertexOne, vertex)) {
                    connectedToOne.put(x, y, z, vertex)
                }
                if (graph.connected(vertexTwo, vertex)) {
                    connectedToTwo.put(x, y, z, vertex)
                }
            }
        }

        if (connectedToOne.values.size > connectedToTwo.values.size) {
            return Pair(connectedToTwo, true)
        } else {
            return Pair(connectedToOne, false)
        }
    }

    override fun merge() {
        TODO("Not yet implemented")
    }
}

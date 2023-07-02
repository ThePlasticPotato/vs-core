package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3d
import org.joml.Vector3i
import org.joml.Vector3ic
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex

class ConnectivityForestImpl(override val graph: ConnGraph,
    override val vertices: HashMap<Vector3ic, BlockPosVertex>,
    override val breakages: MutableSet<ArrayList<Vector3ic?>>,
    override val breakagesToAdd: MutableSet<ArrayList<Vector3ic?>>,
    override val breakagesToRemove: MutableSet<ArrayList<Vector3ic?>>
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

    override fun split(vertlist: ArrayList<Vector3ic?>): MutableSet<Pair<HashMap<Vector3ic, BlockPosVertex>, Vector3ic>> {

        val vertexOne = vertices[vertlist[0]]
        val vertexTwo = vertices[vertlist[1]]
        var vertexThree: BlockPosVertex? = null
        if (vertlist.size > 2) vertexThree = vertices[vertlist[2]]
        var vertexFour: BlockPosVertex? = null
        if (vertlist.size > 3) vertexFour = vertices[vertlist[3]]
        var vertexFive: BlockPosVertex? = null
        if (vertlist.size > 4) vertexFive = vertices[vertlist[4]]
        var vertexSix: BlockPosVertex? = null
        if (vertlist.size > 5) vertexSix = vertices[vertlist[5]]

        val connectedToOne = HashMap<Vector3ic, BlockPosVertex>()
        val connectedToTwo = HashMap<Vector3ic, BlockPosVertex>()
        val connectedToThree = HashMap<Vector3ic, BlockPosVertex>()
        val connectedToFour = HashMap<Vector3ic, BlockPosVertex>()
        val connectedToFive = HashMap<Vector3ic, BlockPosVertex>()
        val connectedToSix = HashMap<Vector3ic, BlockPosVertex>()

        if (vertexOne != null && vertexTwo != null) {
            vertices.forEach{ (vec, vertex) ->
                if (graph.connected(vertexOne, vertex)) {
                    connectedToOne[vec] = vertex
                }
                if (graph.connected(vertexTwo, vertex)) {
                    connectedToTwo[vec] = vertex
                }
                if (vertexThree != null) {
                    if (graph.connected(vertexThree, vertex)) {
                        connectedToThree[vec] = vertex
                    }
                }
                if (vertexFour != null) {
                    if (graph.connected(vertexFour, vertex)) {
                        connectedToFour[vec] = vertex
                    }
                }
                if (vertexFive != null) {
                    if (graph.connected(vertexFive, vertex)) {
                        connectedToFive[vec] = vertex
                    }
                }
                if (vertexSix != null) {
                    if (graph.connected(vertexSix, vertex)) {
                        connectedToSix[vec] = vertex
                    }
                }
            }
        }



        val breaking = mutableSetOf<Pair<HashMap<Vector3ic, BlockPosVertex>, Vector3ic>>()

        breaking.add(Pair(connectedToOne, vertlist[0]!!))
        breaking.add(Pair(connectedToTwo, vertlist[1]!!))
        if (vertexThree != null) {
            breaking.add(Pair(connectedToThree, vertlist[2]!!))
        }
        if (vertexFour != null) {
            breaking.add(Pair(connectedToFour, vertlist[3]!!))
        }
        if (vertexFive != null) {
            breaking.add(Pair(connectedToFive, vertlist[4]!!))
        }
        if (vertexSix != null) {
            breaking.add(Pair(connectedToSix, vertlist[5]!!))
        }

        removeLargestMap(breaking)

        return breaking
    }

    fun removeLargestMap(remover: MutableSet<Pair<HashMap<Vector3ic, BlockPosVertex>, Vector3ic>>) {
        var largest = 0
        var largestMap = Pair(HashMap<Vector3ic, BlockPosVertex>(), Vector3i(0, 0, 0) as Vector3ic)
        for (map in remover) {
            if (map.first.size > largest) {
                largest = map.first.size
                largestMap = map
            }
        }
        remover.remove(largestMap)
    }

    override fun merge() {
        TODO("Not yet implemented")
    }

    override fun verifyIntactOnLoad() {
        for (vertex in vertices.values) {
            for (otherVertex in vertices.values) {
                if (!graph.connected(vertex, otherVertex)) {
                    val toAdd = ArrayList<Vector3ic?>()
                    val vec1 = Vector3i(vertex.posX, vertex.posY, vertex.posZ)
                    val vec2 = Vector3i(otherVertex.posX, otherVertex.posY, otherVertex.posZ)
                    toAdd.add(vec1)
                    toAdd.add(vec2)
                    breakagesToAdd.add(toAdd)
                }
            }
        }
    }

    override fun addToBreakQueue(arr: ArrayList<Vector3ic?>) {
        breakagesToAdd.add(arr)
    }

    override fun removeFromBreakQueue(arr: ArrayList<Vector3ic?>) {
        if (breakages.contains(arr)) {
            breakagesToRemove.remove(arr)
        }
    }

    override fun getBreakQueue(): Set<ArrayList<Vector3ic?>> {
        return breakages.toSet()
    }

    override fun gameTick() {
        if (breakagesToAdd.isNotEmpty()) {
            breakages.addAll(breakagesToAdd)
            breakagesToAdd.clear()
        }
        if (breakagesToRemove.isNotEmpty()) {
            breakages.removeAll(breakagesToRemove)
            breakagesToRemove.clear()
        }
    }
}

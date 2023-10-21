package org.valkyrienskies.core.impl.game.ships

import org.joml.Vector3i
import org.joml.Vector3ic
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.impl.datastructures.dynconn.BlockPosVertex
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnGraph
import org.valkyrienskies.core.impl.datastructures.dynconn.ConnVertex
import org.valkyrienskies.core.impl.hooks.VSEvents
import org.valkyrienskies.core.impl.hooks.VSEvents.AirPocketModifyEvent

class AirPocketForestImpl(
    override val graph: ConnGraph, override val airVertices: HashMap<Vector3ic, BlockPosVertex>,
    public override val outsideAirVertices: HashMap<Vector3ic, BlockPosVertex>,
    override val sealedAirBlocks: HashMap<Vector3ic, BlockPosVertex>,
    override val individualAirPockets: HashMap<Int, HashMap<Vector3ic, BlockPosVertex>>,
    override var currentShipAABB: AABBic,
    override val hostShipId: ShipId
) : AirPocketForest {

    var nextId: Int = 0

    override var shouldUpdateOutsideAir: Boolean = false
    override fun isInAirPocket(posX: Int, posY: Int, posZ: Int): Boolean {
        for (vertex in sealedAirBlocks.values) {
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
            val pocketId = getPocketFromPos(changed.x(), changed.y(), changed.z()) ?: return false
            removeAirPocket(pocketId)
            false
        }
    }

    fun addAirPocket(toAdd: Set<Vector3ic>) {
        val newPocket : HashMap<Vector3ic, BlockPosVertex> = HashMap()
        for (vertex in toAdd) {
            if (airVertices[vertex] == null) {
                continue
            }
            sealedAirBlocks[vertex] = airVertices[vertex]!!
            newPocket[vertex] = airVertices[vertex]!!
        }
        if (newPocket.isEmpty()) return
        mergeAirPockets(newPocket)
    }

    fun removeAirPocket(toRemove: Int) {
        if (individualAirPockets[toRemove] == null) return
        val delete = individualAirPockets[toRemove]!!
        for (vertex in delete.keys) {
            sealedAirBlocks.remove(vertex)
        }
        individualAirPockets.remove(toRemove)
        VSEvents.airPocketModifyEvent.emit(AirPocketModifyEvent(this.hostShipId, toRemove, true))
    }

    fun mergeAirPockets(newPocket: HashMap<Vector3ic, BlockPosVertex>) {
        val toRemoveFromIndividualAirPockets: HashSet<Int> = HashSet()
        val updatedPocket: HashMap<Vector3ic, BlockPosVertex> = HashMap(newPocket)
        var idToUse: Int? = null
        for (sealedAirBlock in newPocket.keys) {
            for (airPocketId in individualAirPockets.keys) {
                val airPocket = individualAirPockets[airPocketId]!!
                var shouldRemove = false
                for (otherSealedAirBlock in airPocket.keys) {
                    if (sealedAirBlock.equals(otherSealedAirBlock)) {
                        shouldRemove = true
                        break
                    }
                }
                if (shouldRemove) {
                    updatedPocket.putAll(airPocket)
                    if (idToUse == null) {
                        idToUse = airPocketId
                    } else {
                        toRemoveFromIndividualAirPockets.add(airPocketId)
                    }
                }
            }
        }
        for (id in toRemoveFromIndividualAirPockets) {
            individualAirPockets.remove(id)
            VSEvents.airPocketModifyEvent.emit(AirPocketModifyEvent(this.hostShipId, id, true))
        }
        if (idToUse == null) {
            idToUse = nextId
            nextId++
            individualAirPockets[idToUse] = updatedPocket
        } else {
            individualAirPockets.replace(idToUse, updatedPocket)
        }
        VSEvents.airPocketModifyEvent.emit(AirPocketModifyEvent(this.hostShipId, idToUse, false))

    }

    fun getPocketFromPos(x: Int, y: Int, z: Int): Int? {
        for (airPocketId in individualAirPockets.keys) {
            val airPocket = individualAirPockets[airPocketId]!!
            for (vertex in airPocket.values) {
                if (vertex.posX == x && vertex.posY == y && vertex.posZ == z) {
                    return airPocketId
                }
            }
        }
        return null
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
                newVertex(vertex.x(), vertex.y(), vertex.z(), true)
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

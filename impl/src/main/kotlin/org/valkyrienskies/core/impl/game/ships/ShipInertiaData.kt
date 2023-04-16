package org.valkyrienskies.core.impl.game.ships

import org.joml.Matrix3d
import org.joml.Matrix3dc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import kotlin.math.abs

/**
 * This class keeps track of a ships mass, center of mass, and moment of inertia given the block changes within the ship.
 */
data class ShipInertiaDataImpl constructor(
    private val _centerOfMassInShip: Vector3d,
    private var _mass: Double,
    private val _momentOfInertiaTensor: Matrix3d
) : ShipInertiaData {
    // Approximate the moment of inertia as a sphere with some value
    private var momentOfInertiaSpherical: Matrix3d = Matrix3d(_momentOfInertiaTensor)

    override val momentOfInertiaTensor: Matrix3dc
        get() = momentOfInertiaSpherical

    override val momentOfInertiaTensorToSave: Matrix3dc
        get() = _momentOfInertiaTensor

    override val centerOfMassInShip: Vector3dc get() = _centerOfMassInShip

    override val mass get() = _mass

    init {
        updateSphericalMOI()
    }

    fun onSetBlock(posX: Int, posY: Int, posZ: Int, oldBlockMass: Double, newBlockMass: Double) {
        val deltaBlockMass = newBlockMass - oldBlockMass
        if (abs(deltaBlockMass) < EPSILON) {
            return // No change in mass
        }

        // Add point masses at each of the 8 points we place mass at
        val addedMassAtEachPoint = deltaBlockMass / 8.0

        addMassAt(posX + INERTIA_OFFSET, posY + INERTIA_OFFSET, posZ + INERTIA_OFFSET, addedMassAtEachPoint)
        addMassAt(posX + INERTIA_OFFSET, posY + INERTIA_OFFSET, posZ - INERTIA_OFFSET, addedMassAtEachPoint)
        addMassAt(posX + INERTIA_OFFSET, posY - INERTIA_OFFSET, posZ + INERTIA_OFFSET, addedMassAtEachPoint)
        addMassAt(posX + INERTIA_OFFSET, posY - INERTIA_OFFSET, posZ - INERTIA_OFFSET, addedMassAtEachPoint)
        addMassAt(posX - INERTIA_OFFSET, posY + INERTIA_OFFSET, posZ + INERTIA_OFFSET, addedMassAtEachPoint)
        addMassAt(posX - INERTIA_OFFSET, posY + INERTIA_OFFSET, posZ - INERTIA_OFFSET, addedMassAtEachPoint)
        addMassAt(posX - INERTIA_OFFSET, posY - INERTIA_OFFSET, posZ + INERTIA_OFFSET, addedMassAtEachPoint)
        addMassAt(posX - INERTIA_OFFSET, posY - INERTIA_OFFSET, posZ - INERTIA_OFFSET, addedMassAtEachPoint)
    }

    // Use sphere MOI because its more stable
    fun onSetBlockUseSphereMOI(posX: Int, posY: Int, posZ: Int, oldBlockMass: Double, newBlockMass: Double) {
        onSetBlock(posX, posY, posZ, oldBlockMass, newBlockMass)
        updateSphericalMOI()
    }

    private fun updateSphericalMOI() {
        val inertiaAlongX = _momentOfInertiaTensor.transform(Vector3d(1.0, 0.0, 0.0)).length()
        val inertiaAlongY = _momentOfInertiaTensor.transform(Vector3d(0.0, 1.0, 0.0)).length()
        val inertiaAlongZ = _momentOfInertiaTensor.transform(Vector3d(0.0, 0.0, 1.0)).length()

        val avgInertia = (inertiaAlongX + inertiaAlongY + inertiaAlongZ) / 3.0
        // TODO: Maybe instead diagonalize the matrix, find its axes, and take the avg along that instead?
        momentOfInertiaSpherical.identity().scale(avgInertia)
    }

    /**
     * Updates the center of mass and rotation inertia tensor matrix of the ShipInertiaData, using the rigid body
     * inertia tensor equations.
     *
     * Reference [http://www.kwon3d.com/theory/moi/triten.html] equations 13 & 14.
     */
    private fun addMassAt(x: Double, y: Double, z: Double, addedMass: Double) {
        // Put the moment of inertia tensor into a double array
        val gameMoITensor = DoubleArray(9)
        val transposed: Matrix3d = _momentOfInertiaTensor.transpose(Matrix3d())
        transposed.get(gameMoITensor)

        val gameTickMass: Double = mass
        val prevCenterOfMass = Vector3d(centerOfMassInShip)
        if (gameTickMass + addedMass > EPSILON) {
            val newCenterOfMass: Vector3d = centerOfMassInShip.mul(gameTickMass, Vector3d())
            newCenterOfMass.add(x * addedMass, y * addedMass, z * addedMass)
            newCenterOfMass.mul(1.0 / (gameTickMass + addedMass))
            _centerOfMassInShip.set(newCenterOfMass)

            // This code is pretty awful in hindsight, but it gets the job done.
            val cmShiftX: Double = prevCenterOfMass.x - centerOfMassInShip.x()
            val cmShiftY: Double = prevCenterOfMass.y - centerOfMassInShip.y()
            val cmShiftZ: Double = prevCenterOfMass.z - centerOfMassInShip.z()
            val rx: Double = x - centerOfMassInShip.x()
            val ry: Double = y - centerOfMassInShip.y()
            val rz: Double = z - centerOfMassInShip.z()
            gameMoITensor[0] = gameMoITensor[0] + (cmShiftY * cmShiftY + cmShiftZ * cmShiftZ) * gameTickMass +
                (ry * ry + rz * rz) * addedMass
            gameMoITensor[1] = gameMoITensor[1] - cmShiftX * cmShiftY * gameTickMass - rx * ry * addedMass
            gameMoITensor[2] = gameMoITensor[2] - cmShiftX * cmShiftZ * gameTickMass - rx * rz * addedMass
            gameMoITensor[3] = gameMoITensor[1]
            gameMoITensor[4] = gameMoITensor[4] + (cmShiftX * cmShiftX + cmShiftZ * cmShiftZ) * gameTickMass +
                (rx * rx + rz * rz) * addedMass
            gameMoITensor[5] = gameMoITensor[5] - cmShiftY * cmShiftZ * gameTickMass - ry * rz * addedMass
            gameMoITensor[6] = gameMoITensor[2]
            gameMoITensor[7] = gameMoITensor[5]
            gameMoITensor[8] = gameMoITensor[8] + (cmShiftX * cmShiftX + cmShiftY * cmShiftY) * gameTickMass +
                (rx * rx + ry * ry) * addedMass
            _momentOfInertiaTensor.set(gameMoITensor).transpose()

            _mass += addedMass
        } else {
            // We have 0 mass, reset mass and moment of inertia to 0
            _centerOfMassInShip.set(x, y, z)
            _momentOfInertiaTensor.zero()
            _mass = 0.0
        }
    }

    fun copyToPhyInertia(): PhysInertia {
        return PhysInertia(mass, Matrix3d(_momentOfInertiaTensor))
    }
    // endregion

    companion object {
        fun newEmptyShipInertiaData(): ShipInertiaDataImpl {
            return ShipInertiaDataImpl(Vector3d(), 0.0, Matrix3d())
        }

        /**
         * Consider 2 doubles to be equal if the distance between them is less than this.
         */
        private const val EPSILON = 1.0e-6

        /**
         * We define the inertia of a cube as being 8 point masses within a cube.
         *
         * The positions of these points are the corners of a different cube with radius [INERTIA_OFFSET].
         */
        private const val INERTIA_OFFSET = 0.4
    }
}

// A deep copy of ShipInertiaData. This is used by the physics pipeline.
data class PhysInertia(val shipMass: Double, val momentOfInertiaTensor: Matrix3dc)

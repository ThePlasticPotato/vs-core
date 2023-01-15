package org.valkyrienskies.core.impl.game.ships

import org.joml.Matrix3d
import org.joml.Matrix4dc
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.Wing
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sin

object WingPhysicsSolver {

    fun applyWingForces(physShip: PhysShip) {
        physShip as PhysShipImpl
        val netShipForce = Vector3d()
        val netShipTorque = Vector3d()

        val shipTransform = physShip.transform
        val vel = physShip.poseVel.vel
        val omega = physShip.poseVel.omega

        physShip.wingManager.forEachWing { wingTransform: Matrix4dc, posX: Int, posY: Int, posZ: Int, wing: Wing ->
            val wingNormalLocal: Vector3dc = wingTransform.transformDirection(Vector3d(wing.wingNormal))

            val localPos: Vector3dc = wingTransform.transformPosition(Vector3d(posX + 0.5, posY + 0.5, posZ + 0.5))
            // Pos relative to center of mass, in global coordinates
            val tDir: Vector3dc =
                shipTransform.shipToWorld.transformPosition(localPos, Vector3d()).sub(shipTransform.positionInWorld)

            // Velocity at the block position, in global coordinates
            val velAtWingGlobal: Vector3dc = (Vector3d(omega).cross(tDir)).add(vel)

            val wingNormalGlobal: Vector3dc = shipTransform.shipToWorld.transformDirection(wingNormalLocal, Vector3d())
            val liftVel: Vector3dc =
                velAtWingGlobal.sub(Vector3d(wingNormalGlobal).mul(wingNormalGlobal.dot(velAtWingGlobal)), Vector3d())

            if (liftVel.lengthSquared() > 1e-12) {
                val liftVelDirection: Vector3dc = Vector3d(liftVel).normalize()
                // Angle of attack, in radians
                val angleOfAttack = liftVelDirection.angle(velAtWingGlobal)

                // println("angleOfAttack is $angleOfAttack")

                val dragDirection = velAtWingGlobal.mul(-1.0, Vector3d())
                if (dragDirection.lengthSquared() < 1e-12) {
                    // Don't normalize, give up
                    return@forEachWing
                }
                dragDirection.normalize()
                dragDirection as Vector3dc
                // val liftVel = velAtWingGlobal.dot(liftVelDirection)

                val liftCoefficient = sin(2.0 * angleOfAttack)
                val liftForceMagnitude = min(wing.wingLiftPower * liftCoefficient * liftVel.lengthSquared(), 1e7)
                // Account for the direction of the wind relative to the wing normal
                val liftForceDirection = -sign(wingNormalGlobal.dot(velAtWingGlobal))
                val liftForceVector: Vector3dc =
                    wingNormalGlobal.mul(liftForceDirection * liftForceMagnitude, Vector3d())

                // TODO: Need to compute [dragCoefficient] more effectively
                val dragCoefficient = liftCoefficient * liftCoefficient
                val dragForceMagnitude = wing.wingDragPower * dragCoefficient * velAtWingGlobal.lengthSquared()
                val dragForceVector: Vector3dc = dragDirection.mul(dragForceMagnitude, Vector3d())

                val totalForce: Vector3dc = liftForceVector.add(dragForceVector, Vector3d())

                if (totalForce.lengthSquared() > 1e16) {
                    // Don't apply it
                    return@forEachWing
                }

                // val localForce = ship.worldToShip.transformDirection(totalForce, Vector3d())
                // val localPos2 = ship.worldToShip.transformDirection(tDir, Vector3d())

                // val tPos: Vector3dc = Vector3d(pos).add( 0.5, 0.5, 0.5).sub(ship!!.transform.positionInShip)
                // physShip.applyRotDependentForceToPos(localForce, tPos)

                val torque = tDir.cross(totalForce, Vector3d())

                netShipTorque.add(torque)
                netShipForce.add(totalForce)
                // physShip.applyInvariantTorque(torque)
                // physShip.applyInvariantForce(totalForce)
            } else {
                // TODO: Do nothing?
            }
        }

        val momentOfInertiaInWorld = Matrix3d().rotate(shipTransform.shipToWorld.getNormalizedRotation(Quaterniond()))
            .mul(physShip._inertia.momentOfInertiaTensor)
        val invMOI = momentOfInertiaInWorld.invert()

        val deltaOmega = invMOI.transform(netShipTorque, Vector3d())

        // Clamp the rotation from [netShipTorque] to be up to 720 degrees per second
        val maxRotation = 4.0 * PI
        if (deltaOmega.length() > maxRotation) {
            netShipTorque.mul(maxRotation / deltaOmega.length())
        }

        physShip.applyInvariantTorque(netShipTorque)
        physShip.applyInvariantForce(netShipForce)
    }
}

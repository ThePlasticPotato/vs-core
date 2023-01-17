package org.valkyrienskies.core.impl.game.ships

import org.joml.Matrix3d
import org.joml.Matrix3dc
import org.joml.Matrix4dc
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.Wing
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.physics_api.PoseVel
import kotlin.math.PI
import kotlin.math.sign
import kotlin.math.sin

object WingPhysicsSolver {

    fun applyWingForces(shipTransform: ShipTransform, poseVel: PoseVel, wingManager: WingManagerImpl, momentOfInertia: Matrix3dc): Pair<Vector3dc, Vector3dc> {
        val netShipForce = Vector3d()
        val netShipTorque = Vector3d()

        val vel = poseVel.vel
        val omega = poseVel.omega

        wingManager.forEachWing { wingTransform: Matrix4dc, posX: Int, posY: Int, posZ: Int, wing: Wing ->
            val wingNormalLocal: Vector3dc = wingTransform.transformDirection(Vector3d(wing.wingNormal))

            val localPos: Vector3dc = wingTransform.transformPosition(Vector3d(posX + 0.5, posY + 0.5, posZ + 0.5))
            // Pos relative to center of mass, in global coordinates
            val tDir: Vector3dc =
                shipTransform.shipToWorld.transformPosition(localPos, Vector3d()).sub(shipTransform.positionInWorld)

            // Velocity at the block position, in global coordinates
            val velAtWingGlobal: Vector3dc = (Vector3d(omega).cross(tDir)).add(vel)

            val wingNormalGlobal: Vector3dc = shipTransform.shipToWorld.transformDirection(wingNormalLocal, Vector3d())
            // Component of [velAtWingGlobal] that is perpendicular to [wingNormalGlobal]
            val liftVel: Vector3dc =
                velAtWingGlobal.sub(Vector3d(wingNormalGlobal).mul(wingNormalGlobal.dot(velAtWingGlobal)), Vector3d())

            if (liftVel.lengthSquared() > 1e-12) {
                val liftVelDirection: Vector3dc = Vector3d(liftVel).normalize()
                // Angle of attack, in radians
                val angleOfAttack = (liftVelDirection.angle(velAtWingGlobal) * -sign(
                    wingNormalGlobal.dot(velAtWingGlobal)
                )) + wing.wingCamberedBiasAngle

                val dragDirection = velAtWingGlobal.mul(-1.0, Vector3d())
                if (dragDirection.lengthSquared() < 1e-12) {
                    // Don't normalize, give up
                    return@forEachWing
                }
                dragDirection.normalize()
                dragDirection as Vector3dc

                val liftCoefficient = sin(2.0 * angleOfAttack)
                val liftForce = (wing.wingLiftPower * liftCoefficient * liftVel.lengthSquared()).coerceIn(-1e7, 1e7)
                val liftForceVector: Vector3dc =
                    wingNormalGlobal.mul(liftForce, Vector3d())

                // TODO: Need to compute [dragCoefficient] more effectively
                val dragCoefficient = liftCoefficient * liftCoefficient
                val dragForceMagnitude = wing.wingDragPower * dragCoefficient * velAtWingGlobal.lengthSquared()
                val dragForceVector: Vector3dc = dragDirection.mul(dragForceMagnitude, Vector3d())

                val totalForce: Vector3dc = liftForceVector.add(dragForceVector, Vector3d())

                if (totalForce.lengthSquared() > 1e16) {
                    // Don't apply it
                    return@forEachWing
                }

                val torque = tDir.cross(totalForce, Vector3d())

                netShipTorque.add(torque)
                netShipForce.add(totalForce)
            } else {
                // TODO: Do nothing?
            }
        }

        val invMOI = momentOfInertia.invert(Matrix3d())

        // Compute the change in omega, in the ship's local coordinate system
        val deltaOmegaInLocal =
            invMOI.transform(shipTransform.worldToShip.transformDirection(netShipTorque), Vector3d())

        // Clamp the rotation from [netShipTorque] to be up to 720 degrees per second
        val maxRotation = 4.0 * PI
        if (deltaOmegaInLocal.length() > maxRotation) {
            netShipTorque.mul(maxRotation / deltaOmegaInLocal.length())
        }

        return Pair(netShipForce, netShipTorque)
    }
}

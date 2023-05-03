package org.valkyrienskies.core.api.bodies

import org.joml.Vector3dc

interface PhysicsVSBody : ServerBaseVSBody {

    companion object {
        const val REASON_UNKNOWN = "unspecified"
    }

    fun applyForceInWorld(force: Vector3dc) = applyForceInWorld(REASON_UNKNOWN, force)
    fun applyForceInLocal(force: Vector3dc) = applyForceInLocal(REASON_UNKNOWN, force)
    fun applyForceInWorld(force: Vector3dc, pos: Vector3dc) = applyForceInWorld(REASON_UNKNOWN, force)
    fun applyForceInLocal(force: Vector3dc, pos: Vector3dc) = applyForceInLocal(REASON_UNKNOWN, force)

    fun applyRotatingForceInWorld(force: Vector3dc) = applyRotatingForceInWorld(REASON_UNKNOWN, force)
    fun applyRotatingForceInLocal(force: Vector3dc) = applyRotatingForceInLocal(REASON_UNKNOWN, force)
    fun applyRotatingForceInWorld(force: Vector3dc, pos: Vector3dc) = applyRotatingForceInWorld(REASON_UNKNOWN, force)
    fun applyRotatingForceInLocal(force: Vector3dc, pos: Vector3dc) = applyRotatingForceInLocal(REASON_UNKNOWN, force)

    fun applyTorqueInWorld(torque: Vector3dc) = applyTorqueInWorld(REASON_UNKNOWN, torque)
    fun applyTorqueInLocal(torque: Vector3dc) = applyTorqueInLocal(REASON_UNKNOWN, torque)
    fun applyRotatingTorqueInWorld(torque: Vector3dc) = applyRotatingTorqueInWorld(REASON_UNKNOWN, torque)
    fun applyRotatingTorqueInLocal(torque: Vector3dc) = applyRotatingTorqueInLocal(REASON_UNKNOWN, torque)


    fun applyForceInWorld(reason: String, force: Vector3dc)
    fun applyForceInLocal(reason: String, force: Vector3dc)
    fun applyForceInWorld(reason: String, force: Vector3dc, pos: Vector3dc)
    fun applyForceInLocal(reason: String, force: Vector3dc, pos: Vector3dc)

    fun applyRotatingForceInWorld(reason: String, force: Vector3dc)
    fun applyRotatingForceInLocal(reason: String, force: Vector3dc)
    fun applyRotatingForceInWorld(reason: String, force: Vector3dc, pos: Vector3dc)
    fun applyRotatingForceInLocal(reason: String, force: Vector3dc, pos: Vector3dc)

    fun applyTorqueInWorld(reason: String, torque: Vector3dc)
    fun applyTorqueInLocal(reason: String, torque: Vector3dc)
    fun applyRotatingTorqueInWorld(reason: String, torque: Vector3dc)
    fun applyRotatingTorqueInLocal(reason: String, torque: Vector3dc)

}

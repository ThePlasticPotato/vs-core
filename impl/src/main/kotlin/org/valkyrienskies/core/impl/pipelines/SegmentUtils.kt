package org.valkyrienskies.core.impl.pipelines

import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.physics_api.*

@Deprecated("Segments don't exist anymore")
object SegmentUtils {
    fun createSegmentDisplacementFromScaling(dimension: Int, scaling: Double): SegmentDisplacement {
        return SegmentDisplacement(PoseVel.NULL_POSE_VEL, scaling, dimension)
    }

    fun createSegmentTrackerFromScaling(dimension: Int, scaling: Double): SegmentTracker {
        return SingleSegmentTracker()
    }

    fun transformPos(
        poseVel: PoseVel, segmentDisplacement: SegmentDisplacement, pos: Vector3dc, dest: Vector3d
    ): Vector3d {
        poseVel.transformPosition(pos, dest)
        segmentDisplacement.transformPosition(dest)
        return dest
    }

    fun invTransformPos(
        poseVel: PoseVel, segmentDisplacement: SegmentDisplacement, pos: Vector3dc, dest: Vector3d
    ): Vector3d {
        segmentDisplacement.invTransformPosition(pos, dest)
        poseVel.invTransformPosition(dest)
        return dest
    }

    fun transformDirectionWithScale(
        poseVel: PoseVel, segmentDisplacement: SegmentDisplacement, dir: Vector3dc, dest: Vector3d
    ): Vector3d {
        poseVel.transformDirection(dir, dest)
        segmentDisplacement.transformDirectionWithScale(dest)
        return dest
    }

    fun invTransformDirectionWithScale(
        poseVel: PoseVel, segmentDisplacement: SegmentDisplacement, dir: Vector3dc, dest: Vector3d
    ): Vector3d {
        segmentDisplacement.invTransformDirectionWithScale(dir, dest)
        poseVel.invTransformDirection(dest)
        return dest
    }

    fun transformDirectionWithoutScale(
        poseVel: PoseVel, segmentDisplacement: SegmentDisplacement, dir: Vector3dc, dest: Vector3d
    ): Vector3d {
        poseVel.transformDirection(dir, dest)
        segmentDisplacement.transformDirectionWithoutScale(dest)
        return dest
    }

    fun invTransformDirectionWithoutScale(
        poseVel: PoseVel, segmentDisplacement: SegmentDisplacement, dir: Vector3dc, dest: Vector3d
    ): Vector3d {
        segmentDisplacement.invTransformDirectionWithoutScale(dir, dest)
        poseVel.invTransformDirection(dest)
        return dest
    }

    fun getOmega(poseVel: PoseVel, segmentDisplacement: SegmentDisplacement, dest: Vector3d): Vector3d {
        dest.set(poseVel.omega)
        segmentDisplacement.transformDirectionWithoutScale(dest)
        dest.add(segmentDisplacement.getPoseVelForInternalUseOnly().omega)
        return dest
    }

    fun getVelocity(poseVel: PoseVel, segmentDisplacement: SegmentDisplacement, dest: Vector3d): Vector3d {
        dest.set(poseVel.vel)
        segmentDisplacement.transformDirectionWithScale(dest)
        dest.add(segmentDisplacement.getPoseVelForInternalUseOnly().vel)
        return dest
    }
}

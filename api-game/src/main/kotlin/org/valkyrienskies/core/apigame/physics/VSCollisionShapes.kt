package org.valkyrienskies.core.apigame.physics

import org.joml.Vector3ic
import org.joml.primitives.AABBic
import org.valkyrienskies.core.apigame.physics.VSCollisionShapes.BOX
import org.valkyrienskies.core.apigame.physics.VSCollisionShapes.CAPSULE
import org.valkyrienskies.core.apigame.physics.VSCollisionShapes.SPHERE
import org.valkyrienskies.core.apigame.physics.VSCollisionShapes.VOXEL
import org.valkyrienskies.core.apigame.physics.VSCollisionShapes.WHEEL

enum class VSCollisionShapes {
    VOXEL, SPHERE, WHEEL, BOX, CAPSULE
}

interface VSCollisionShapeData {
    val shapeType: VSCollisionShapes
}

data class VSVoxelCollisionShapeData(
    val minDefined: Vector3ic,
    val maxDefined: Vector3ic,
    val totalVoxelRegion: AABBic,
    val shipVoxelsFullyLoaded: Boolean,
) : VSCollisionShapeData {
    override val shapeType: VSCollisionShapes
        get() = VOXEL
}

data class VSSphereCollisionShapeData(
    val radius: Double
) : VSCollisionShapeData {
    override val shapeType: VSCollisionShapes
        get() = SPHERE
}

data class VSWheelCollisionShapeData(
    val wheelRadius: Double,
    val halfThickness: Double,
) : VSCollisionShapeData {
    override val shapeType: VSCollisionShapes
        get() = WHEEL
}

data class VSBoxCollisionShapeData(
    val lengthX: Double,
    val lengthY: Double,
    val lengthZ: Double,
) : VSCollisionShapeData {
    override val shapeType: VSCollisionShapes
        get() = BOX
}

data class VSCapsuleCollisionShapeData(
    val radius: Double,
    val length: Double,
) : VSCollisionShapeData {
    override val shapeType: VSCollisionShapes
        get() = CAPSULE
}

package org.valkyrienskies.core

import org.joml.Matrix3d
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.ships.properties.ChunkClaim
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.api.ships.saveAttachment
import org.valkyrienskies.core.chunk_tracking.ShipActiveChunksSet
import org.valkyrienskies.core.datastructures.IBlockPosSet
import org.valkyrienskies.core.datastructures.SmallBlockPosSet
import org.valkyrienskies.core.datastructures.SmallBlockPosSetAABB
import org.valkyrienskies.core.game.ChunkClaimImpl
import org.valkyrienskies.core.game.ships.*
import org.valkyrienskies.core.game.ships.serialization.shipinertia.dto.ShipInertiaDataV0
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV0
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV3
import org.valkyrienskies.core.game.ships.serialization.shiptransform.dto.ShipTransformDataV0
import org.valkyrienskies.core.pipelines.ShipInPhysicsFrameData
import org.valkyrienskies.physics_api.*
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.asJavaRandom

/**
 * This singleton generates random objects for unit tests.
 */
object VSRandomUtils {

    /**
     * Seeded random for tests
     */
    val defaultRandom = Random(-945798382)

    /**
     * Shares the same seed and state as defaultRandom but it's java.util.Random
     * instead of kotlin.random.Random
     */
    val defaultRandomJava = defaultRandom.asJavaRandom()

    /**
     * Use this instead of random.nextDouble() to avoid overflow errors
     */
    @Suppress("WeakerAccess")
    fun randomDoubleNotCloseToLimit(random: Random = defaultRandom): Double {
        return random.nextDouble(-1000000.0, 1000000.0)
    }

    /**
     * Use this instead of random.nextInt() to avoid overflow errors
     */
    @Suppress("WeakerAccess")
    fun randomIntegerNotCloseToLimit(random: Random = defaultRandom): Int {
        return random.nextInt(-1000000, 1000000)
    }

    @Suppress("WeakerAccess")
    fun randomVector3d(random: Random = defaultRandom): Vector3d {
        return Vector3d(
            randomDoubleNotCloseToLimit(random),
            randomDoubleNotCloseToLimit(random),
            randomDoubleNotCloseToLimit(random)
        )
    }

    /**
     * Generates a random unit quaternion with a uniform distribution.
     */
    @Suppress("WeakerAccess")
    fun randomQuaterniond(random: Random = defaultRandom): Quaterniond {
        // First generate a random unit vector
        // We use the gaussian distribution to make the random unit vector distribution uniform
        var randX = random.nextGaussian()
        var randY = random.nextGaussian()
        var randZ = random.nextGaussian()
        val normalizationConstant = sqrt(randX * randX + randY * randY + randZ * randZ)

        // Edge case
        if (normalizationConstant < 1.0e-6) {
            return Quaterniond()
        }

        // Then normalize these to form a unit vector
        randX /= normalizationConstant
        randY /= normalizationConstant
        randZ /= normalizationConstant

        // Then generate a random rotation degree
        val randomDegrees = random.nextDouble(360.0)

        // Finally generate a quaternion from the random axis and random angle
        return Quaterniond().fromAxisAngleDeg(randX, randY, randZ, randomDegrees)
    }

    @Suppress("WeakerAccess")
    fun randomMatrix3d(random: Random = defaultRandom): Matrix3d {
        return Matrix3d().set(randomQuaterniond(random))
    }

    @Suppress("WeakerAccess")
    fun randomString(random: Random = defaultRandom, length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { charPool.random(random) }
            .joinToString("")
    }

    @Suppress("WeakerAccess")
    fun randomChunkClaim(random: Random = defaultRandom): ChunkClaim {
        return ChunkClaimImpl.getClaim(randomIntegerNotCloseToLimit(random), randomIntegerNotCloseToLimit(random))
    }

    @Suppress("WeakerAccess")
    fun randomShipPhysicsData(random: Random = defaultRandom): ShipPhysicsData {
        return ShipPhysicsData(randomVector3d(random), randomVector3d(random))
    }

    @Suppress("WeakerAccess")
    fun randomShipInertiaData(random: Random = defaultRandom): ShipInertiaDataImpl {
        return ShipInertiaDataImpl(randomVector3d(random), randomDoubleNotCloseToLimit(random), randomMatrix3d(random))
    }

    fun randomShipInertiaDataV0(random: Random = defaultRandom): ShipInertiaDataV0 {
        return ShipInertiaDataV0(randomVector3d(random), randomDoubleNotCloseToLimit(random), randomMatrix3d(random))
    }

    @Suppress("WeakerAccess")
    fun randomShipTransform(random: Random = defaultRandom): ShipTransform {
        val scalingMaxMagnitude = 10.0
        val randomScaling = Vector3d(
            random.nextDouble(-scalingMaxMagnitude, scalingMaxMagnitude),
            random.nextDouble(-scalingMaxMagnitude, scalingMaxMagnitude),
            random.nextDouble(-scalingMaxMagnitude, scalingMaxMagnitude)
        )
        return ShipTransformImpl(randomVector3d(random), randomVector3d(random), randomQuaterniond(random), randomScaling)
    }

    @Suppress("WeakerAccess")
    fun randomShipTransformDataV0(random: Random = defaultRandom): ShipTransformDataV0 {
        val scalingMaxMagnitude = 10.0
        val randomScaling = Vector3d(
            random.nextDouble(-scalingMaxMagnitude, scalingMaxMagnitude),
            random.nextDouble(-scalingMaxMagnitude, scalingMaxMagnitude),
            random.nextDouble(-scalingMaxMagnitude, scalingMaxMagnitude)
        )
        return ShipTransformDataV0(randomVector3d(random), randomVector3d(random), randomQuaterniond(random), randomScaling)
    }

    @Suppress("WeakerAccess")
    fun randomAABBd(random: Random = defaultRandom): AABBd {
        return AABBd(
            randomDoubleNotCloseToLimit(random),
            randomDoubleNotCloseToLimit(random),
            randomDoubleNotCloseToLimit(random),
            randomDoubleNotCloseToLimit(random),
            randomDoubleNotCloseToLimit(random),
            randomDoubleNotCloseToLimit(random)
        ).correctBounds()
    }

    @Suppress("WeakerAccess")
    fun randomBlockPosSetAABB(random: Random = defaultRandom, size: Int): SmallBlockPosSetAABB {
        val centerX = randomIntegerNotCloseToLimit(random)
        val centerY = randomIntegerNotCloseToLimit(random)
        val centerZ = randomIntegerNotCloseToLimit(random)
        val blockPosSet = SmallBlockPosSetAABB(centerX, centerY, centerZ, 4096, 256, 4096)
        fillBlockPosSet(random, blockPosSet, centerX, centerY, centerZ, size)
        return blockPosSet
    }

    @Suppress("WeakerAccess")
    fun randomBlockPosSet(random: Random = defaultRandom, size: Int): SmallBlockPosSet {
        val centerX = randomIntegerNotCloseToLimit(random)
        val centerY = randomIntegerNotCloseToLimit(random)
        val centerZ = randomIntegerNotCloseToLimit(random)
        val blockPosSet = SmallBlockPosSet(centerX, centerY, centerZ)
        fillBlockPosSet(random, blockPosSet, centerX, centerY, centerZ, size)
        return blockPosSet
    }

    private fun fillBlockPosSet(
        random: Random = defaultRandom,
        blockPosSet: IBlockPosSet,
        centerX: Int,
        centerY: Int,
        centerZ: Int,
        size: Int
    ) {
        for (i in 1 until size) {
            val x = random.nextInt(-2048, 2047) + centerX
            val y = random.nextInt(-128, 127) + centerY
            val z = random.nextInt(-2048, 2047) + centerZ
            blockPosSet.add(x, y, z)
        }
    }

    @Suppress("WeakerAccess")
    fun randomShipData(random: Random = defaultRandom): ShipData {
        return ShipData(
            id = randomShipId(),
            name = randomString(random, random.nextInt(10)),
            chunkClaim = randomChunkClaim(random),
            chunkClaimDimension = randomString(random, random.nextInt(10)),
            physicsData = randomShipPhysicsData(random),
            inertiaData = randomShipInertiaData(random),
            shipTransform = randomShipTransform(random),
            prevTickShipTransform = randomShipTransform(random),
            shipAABB = randomAABBd(random),
            shipVoxelAABB = null,
            shipActiveChunksSet = randomShipActiveChunkSet(random, random.nextInt(100))
        ).apply { saveAttachment(random.nextInt()) }
    }

    @Suppress("WeakerAccess")
    fun randomQueryableShipData(random: Random = defaultRandom, size: Int): MutableQueryableShipDataServer {
        val queryableShipData = QueryableShipDataImpl<ShipData>()
        for (i in 1..size) {
            queryableShipData.addShipData(randomShipData(random))
        }
        return queryableShipData
    }

    @Suppress("WeakerAccess")
    fun randomShipActiveChunkSet(random: Random = defaultRandom, size: Int): ShipActiveChunksSet {
        val shipActiveChunkSet = ShipActiveChunksSet.create()
        for (i in 1..size) {
            shipActiveChunkSet.add(randomIntegerNotCloseToLimit(random), randomIntegerNotCloseToLimit(random))
        }
        return shipActiveChunkSet
    }

    @Suppress("WeakerAccess")
    fun randomShipId(): ShipId = Random.nextLong()

    @Suppress("WeakerAccess")
    fun randomShipInPhysicsFrame(id: ShipId = randomShipId()): ShipInPhysicsFrameData =
        ShipInPhysicsFrameData(
            id,
            RigidBodyInertiaData(
                Random.nextDouble(),
                randomMatrix3d()
            ),
            PoseVel(
                randomVector3d(),
                randomQuaterniond(),
                randomVector3d(),
                randomVector3d()
            ),
            SingleSegmentTracker(Segment(0, SegmentDisplacement(PoseVel.NULL_POSE_VEL, 1.0, 0))),
            randomVector3d(),
            randomAABBd()
        )

    fun Random.nextGaussian(): Double {
        // See Knuth, TAOCP, Vol. 2, 3rd edition, Section 3.4.1 Algorithm C.
        var v1: Double
        var v2: Double
        var s: Double
        do {
            v1 = 2 * nextDouble() - 1 // between -1 and 1
            v2 = 2 * nextDouble() - 1 // between -1 and 1
            s = v1 * v1 + v2 * v2
        } while (s >= 1 || s == 0.0)
        val multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s)
        return v1 * multiplier
    }

    fun randomServerShipDataV0(random: Random = defaultRandom): ServerShipDataV0 {
        return ServerShipDataV0(
            id = randomShipId(),
            name = randomString(random, random.nextInt(10)),
            chunkClaim = randomChunkClaim(random),
            chunkClaimDimension = randomString(random, random.nextInt(10)),
            physicsData = randomShipPhysicsData(random),
            inertiaData = randomShipInertiaDataV0(random),
            shipTransform = randomShipTransformDataV0(random),
            prevTickShipTransform = randomShipTransformDataV0(random),
            shipAABB = randomAABBd(random),
            shipVoxelAABB = null,
            shipActiveChunksSet = randomShipActiveChunkSet(random, random.nextInt(100))
        )
    }

    fun randomServerShipDataV3(random: Random = defaultRandom): ServerShipDataV3 {
        return ServerShipDataV3(
            id = randomShipId(),
            name = randomString(random, random.nextInt(10)),
            chunkClaim = randomChunkClaim(random),
            chunkClaimDimension = randomString(random, random.nextInt(10)),
            velocity = randomVector3d(random),
            omega = randomVector3d(random),
            inertiaData = randomShipInertiaDataV0(random),
            transform = randomShipTransformDataV0(random),
            prevTickTransform = randomShipTransformDataV0(random),
            worldAABB = randomAABBd(random),
            shipAABB = null,
            activeChunks = randomShipActiveChunkSet(random, random.nextInt(100)),
            isStatic = random.nextBoolean(),
            persistentAttachedData = HashMap()
        )
    }
}

package org.valkyrienskies.test_utils.generators

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import org.valkyrienskies.core.impl.VSRandomUtils
import kotlin.random.Random

fun <T> fromVsRandom(generator: (Random) -> T): Arb<T> =
    arbitrary { rs -> generator(rs.random) }

fun Arb.Companion.shipData() = fromVsRandom(VSRandomUtils::randomShipData)

fun Arb.Companion.inertiaData() = fromVsRandom(VSRandomUtils::randomShipInertiaData)

fun Arb.Companion.queryableShipData(size: Arb<Int>) = arbitrary { rs ->
    VSRandomUtils.randomQueryableShipData(rs.random, size.bind())
}

fun Arb.Companion.serverShipDataV0() = fromVsRandom(VSRandomUtils::randomServerShipDataV0)

fun Arb.Companion.serverShipDataV3() = fromVsRandom(VSRandomUtils::randomServerShipDataV3)

fun Arb.Companion.chunkClaim() = fromVsRandom(VSRandomUtils::randomChunkClaim)

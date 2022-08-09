package org.valkyrienskies.test_utils.generators

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import org.valkyrienskies.core.VSRandomUtils
import kotlin.random.Random

fun <T> fromVsRandom(generator: (Random) -> T): Arb<T> =
    arbitrary { rs -> generator(rs.random) }

fun Arb.Companion.shipData() = fromVsRandom(VSRandomUtils::randomShipData)

fun Arb.Companion.queryableShipData(size: Arb<Int>) = arbitrary { rs ->
    VSRandomUtils.randomQueryableShipData(rs.random, size.bind())
}

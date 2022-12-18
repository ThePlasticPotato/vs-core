package org.valkyrienskies.test_utils.generators

import io.kotest.property.Arb
import io.kotest.property.arbitrary.doubleArray
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.exhaustive.exhaustive

fun Arb.Companion.doubleArray(length: Int, content: Arb<Double> = numericDouble()) =
    doubleArray(listOf(length).exhaustive(), content)

/**
 * Returns a random int that isn't closer to [Int.MIN_VALUE] or [Int.MAX_VALUE] than [distFromMax]
 *
 * i.e. ints in the range: `Int.MIN_VALUE + distFromMax <= x <= Int.MAX_VALUE - distFromMax`
 */
fun Arb.Companion.intNotNearLimit(distFromMax: Int) = int(Int.MIN_VALUE + distFromMax, Int.MAX_VALUE - distFromMax)

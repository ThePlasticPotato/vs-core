package org.valkyrienskies.test_utils.generators

import io.kotest.property.Arb
import io.kotest.property.arbitrary.doubleArray
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.exhaustive.exhaustive

fun Arb.Companion.doubleArray(length: Int, content: Arb<Double> = numericDouble()) =
    doubleArray(listOf(length).exhaustive(), content)

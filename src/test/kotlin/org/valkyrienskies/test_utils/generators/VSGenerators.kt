package org.valkyrienskies.test_utils.generators

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import org.valkyrienskies.core.VSRandomUtils

fun Arb.Companion.shipData() = arbitrary { rs ->
    VSRandomUtils.randomShipData(rs.random)
}

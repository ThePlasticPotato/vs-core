package org.valkyrienskies.core.datastructures

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.valkyrienskies.test_utils.generators.intNotNearLimit

class BlockPosSetAABBGeneratorTest2 : StringSpec({

    "accepts any coordinates without crashing within size" {
        val maxSize = 100_000

        checkAll(
            Arb.intNotNearLimit(maxSize), Arb.intNotNearLimit(maxSize), Arb.intNotNearLimit(maxSize),
            Arb.int(2..maxSize), Arb.int(2..maxSize), Arb.int(2..maxSize)
        ) { centerX, centerY, centerZ, xSize, ySize, zSize ->
            val gen = BlockPosSetAABBGenerator(centerX, centerY, centerZ, xSize, ySize, zSize)

            checkAll(
                Arb.int(centerX - (xSize / 2) until centerX + (xSize / 2)),
                Arb.int(centerY - (ySize / 2) until centerY + (ySize / 2)),
                Arb.int(centerZ - (zSize / 2) until centerZ + (zSize / 2)),
                Arb.boolean()
            ) { x, y, z, shouldRemove ->
                if (shouldRemove) {
                    gen.remove(x, y, z)
                } else {
                    gen.add(x, y, z)
                }

                gen.makeAABB()
            }

        }

    }

})

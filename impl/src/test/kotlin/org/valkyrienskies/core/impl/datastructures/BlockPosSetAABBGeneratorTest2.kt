package org.valkyrienskies.core.impl.datastructures

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.valkyrienskies.test_utils.generators.intNotNearLimit

class BlockPosSetAABBGeneratorTest2 : StringSpec({

    "accepts any coordinates without crashing within size" {
        val maxSize = 100_000

        checkAll(
            iterations = 200,
            Arb.intNotNearLimit(maxSize), Arb.intNotNearLimit(maxSize), Arb.intNotNearLimit(maxSize),
            Arb.int(2..maxSize), Arb.int(2..maxSize), Arb.int(2..maxSize)
        ) { centerX, centerY, centerZ, xSize, ySize, zSize ->
            val gen = BlockPosSetAABBGenerator(
                centerX,
                centerY,
                centerZ,
                xSize,
                ySize,
                zSize
            )
            val testGen =
                ExtremelyNaiveVoxelFieldAABBMaker(
                    centerX,
                    centerZ
                )

            checkAll(
                iterations = 200,
                Arb.int(centerX - (xSize / 2) until centerX + (xSize / 2)),
                Arb.int(centerY - (ySize / 2) until centerY + (ySize / 2)),
                Arb.int(centerZ - (zSize / 2) until centerZ + (zSize / 2)),
                Arb.boolean()
            ) { x, y, z, shouldRemove ->
                if (shouldRemove) {
                    testGen.removeVoxel(x, y, z)
                    gen.remove(x, y, z)
                } else {
                    testGen.addVoxel(x, y, z)
                    gen.add(x, y, z)
                }

                gen.makeAABB() shouldBe testGen.makeVoxelFieldAABB()
            }

        }

    }

    "dynamic generator add-only property test" {
        val gen = DynamicBlockPosSetAABB()
        val testGen =
            ExtremelyNaiveVoxelFieldAABBMaker(
                0,
                0
            )
        checkAll(Arb.int(), Arb.int(), Arb.int()) { x, y, z ->
            gen.add(x, y, z)
            testGen.addVoxel(x, y, z)
            gen.makeAABB() shouldBe testGen.makeVoxelFieldAABB()
        }
    }

})

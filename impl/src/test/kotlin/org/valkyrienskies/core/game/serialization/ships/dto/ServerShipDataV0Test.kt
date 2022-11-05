package org.valkyrienskies.core.game.serialization.ships.dto

import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.joml.Vector3d
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV0
import org.valkyrienskies.core.util.serialization.VSJacksonUtil
import java.nio.file.Files
import java.nio.file.Paths

class ServerShipDataV0Test : StringSpec({

    "can deserialize from legacy shipdata" {
        val value = VSJacksonUtil.defaultMapper.readValue<ServerShipDataV0>(
            Files.newInputStream(Paths.get("./src/test/resources/shipdata_legacy_example1.dat"))
        )

        value.name shouldBe "ship-example-1"
        value.id shouldBe 1L
        value.chunkClaim shouldBe ChunkClaim(1, 2)
        value.chunkClaimDimension shouldBe "example-dimension"
        value.shipTransform.shipPositionInWorldCoordinates shouldBe Vector3d(1.0, 2.0, 3.0)
        value.shipTransform.shipPositionInShipCoordinates shouldBe Vector3d(-1.0, -2.0, -3.0)

    }
})

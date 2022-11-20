package org.valkyrienskies.core.program

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.valkyrienskies.test_utils.fakes.FakeVSCoreFactory
import java.nio.file.Files
import java.nio.file.Paths

class VSPipelineSerializationTest : StringSpec({

    val core = FakeVSCoreFactory.fakeVsCoreServer()

    "serialize and deserialize a pipeline from legacy data and the current serialization format" {
        val shipDataBytes = withContext(Dispatchers.IO) {
            Files.readAllBytes(Paths.get("src/test/resources/queryable_ship_data_legacy.dat"))
        }

        val chunkAllocatorBytes = withContext(Dispatchers.IO) {
            Files.readAllBytes(Paths.get("src/test/resources/chunk_allocator_legacy.dat"))
        }

        // deserialize from legacy data
        val pipeline = core.newPipelineLegacyData(shipDataBytes, chunkAllocatorBytes)

        // reserialize
        val serialized = core.serializePipeline(pipeline)
        val deserialized = core.newPipeline(serialized)

        deserialized.shipWorld.allShips shouldContainExactly pipeline.shipWorld.allShips
        deserialized.shipWorld.chunkAllocator shouldBe pipeline.shipWorld.chunkAllocator

        // do it again just to make sure

        val serialized2 = core.serializePipeline(deserialized)
        val deserialized2 = core.newPipeline(serialized2)

        deserialized2.shipWorld.allShips shouldContainExactly pipeline.shipWorld.allShips
        deserialized.shipWorld.chunkAllocator shouldBe pipeline.shipWorld.chunkAllocator
    }

})

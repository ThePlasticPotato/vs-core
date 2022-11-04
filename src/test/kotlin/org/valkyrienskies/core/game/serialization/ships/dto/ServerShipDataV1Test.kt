package org.valkyrienskies.core.game.serialization.ships.dto

import com.google.common.collect.MutableClassToInstanceMap
import dagger.Component
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.valkyrienskies.core.VSRandomUtils
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV1
import org.valkyrienskies.core.game.ships.serialization.shipserver.dto.ServerShipDataV1Updater
import org.valkyrienskies.core.game.ships.serialization.vspipeline.VSPipelineSerializer
import org.valkyrienskies.core.program.VSCoreModule
import javax.inject.Singleton

class ServerShipDataV1Test : StringSpec({
    val component = DaggerServerShipDataV1TestComponent.create()

    "can upgrade from v1 to v2" {
        // create a test instance of ServerShipDataV1
        val v1 = ServerShipDataV1(
            id = 0,
            name = "test",
            chunkClaim = VSRandomUtils.randomChunkClaim(),
            chunkClaimDimension = "fake_dimension",
            physicsData = VSRandomUtils.randomShipPhysicsData(),
            inertiaData = VSRandomUtils.randomShipInertiaData(),
            shipTransform = VSRandomUtils.randomShipTransform(),
            prevTickShipTransform = VSRandomUtils.randomShipTransform(),
            shipAABB = VSRandomUtils.randomAABBd(),
            shipVoxelAABB = null,
            shipActiveChunksSet = VSRandomUtils.randomShipActiveChunkSet(size = 10),
            isStatic = false,
            persistentAttachedData = MutableClassToInstanceMap.create(),
        )

        val v2 = component.v1Updater().update(v1)

        v1.id shouldBe v2.id
        v1.name shouldBe v2.name
        v1.chunkClaim shouldBe v2.chunkClaim
        v1.chunkClaimDimension shouldBe v2.chunkClaimDimension
        v1.physicsData shouldBe v2.physicsData
        v1.inertiaData shouldBe v2.inertiaData
        v1.shipTransform shouldBe v2.shipTransform
        v1.prevTickShipTransform shouldBe v2.prevTickShipTransform
        v1.shipAABB shouldBe v2.shipAABB
        v1.shipVoxelAABB shouldBe v2.shipVoxelAABB
        v1.shipActiveChunksSet shouldBe v2.shipActiveChunksSet
        v1.isStatic shouldBe v2.isStatic
        v1.persistentAttachedData shouldContainExactly v2.persistentAttachedData
    }

})

@Singleton
@Component(modules = [VSCoreModule::class])
internal interface ServerShipDataV1TestComponent {
    fun v1Updater(): ServerShipDataV1Updater
}

@Singleton
@Component(modules = [VSCoreModule::class])
internal interface VSPipelineSerializerComponent {
    fun newPipelineSerializer(): VSPipelineSerializer
}

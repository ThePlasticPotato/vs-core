package org.valkyrienskies.core.chunk_tracking

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.valkyrienskies.core.config.VSCoreConfig
import org.valkyrienskies.test_utils.VSBlankUtils
import org.valkyrienskies.test_utils.fakes.FakePlayer

class ShipObjectServerWorldChunkTrackerTest : StringSpec({

    "track" {
        val dimension = "fake_dimension"
        val ship = VSBlankUtils.blankShipData(chunkClaimDimension = dimension)
        ship.shipActiveChunksSet.addChunkPos(0, 0)
        val ships = listOf(ship)

        val player = FakePlayer(dimension = dimension)
        val players = setOf(player)

        val config = VSCoreConfig.Server()
        config.shipLoadDistance = 50.0
        config.shipUnloadDistance = 100.0

        val tracker = ShipObjectServerWorldChunkTracker(config)

        // Single player at same coordinate as chunk should watch it
        run {
            val (watchTasks, unwatchTasks) = tracker.generateChunkWatchTasksAndUpdatePlayers(
                players, players, ships, listOf()
            )

            watchTasks shouldHaveSize 1
            unwatchTasks shouldHaveSize 0
            val task = watchTasks.first()

            task.getChunkX() shouldBe 0
            task.getChunkZ() shouldBe 0

            val trackingInfo = tracker.applyTasksAndGenerateTrackingInfo(watchTasks, unwatchTasks)

            trackingInfo.playersToShipsNewlyWatchingMap shouldHaveSize 1
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 1
            trackingInfo.playersToShipsNoLongerWatchingMap shouldHaveSize 0
            trackingInfo.shipsToLoad shouldHaveSize 1
            trackingInfo.shipsToUnload shouldHaveSize 0
        }

        // nothing changes, there should be no watch task and there should be no players newly watching
        run {
            val (watchTasks, unwatchTasks) = tracker.generateChunkWatchTasksAndUpdatePlayers(
                players, players, ships, listOf()
            )

            watchTasks shouldHaveSize 0
            unwatchTasks shouldHaveSize 0

            val trackingInfo = tracker.applyTasksAndGenerateTrackingInfo(watchTasks, unwatchTasks)

            trackingInfo.playersToShipsNewlyWatchingMap shouldHaveSize 0
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 1
            trackingInfo.playersToShipsNoLongerWatchingMap shouldHaveSize 0
            trackingInfo.shipsToLoad shouldHaveSize 0
            trackingInfo.shipsToUnload shouldHaveSize 0
        }

        // let's move the player out. They should be 50.1 blocks from the edge of the chunk now
        player.position.set(66.1, 0.0, 0.0)

        // still nothing changes
        run {
            val (watchTasks, unwatchTasks) = tracker.generateChunkWatchTasksAndUpdatePlayers(
                players, players, ships, listOf()
            )

            watchTasks shouldHaveSize 0
            unwatchTasks shouldHaveSize 0

            val trackingInfo = tracker.applyTasksAndGenerateTrackingInfo(watchTasks, unwatchTasks)

            trackingInfo.playersToShipsNewlyWatchingMap shouldHaveSize 0
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 1
            trackingInfo.playersToShipsNoLongerWatchingMap shouldHaveSize 0
            trackingInfo.shipsToLoad shouldHaveSize 0
            trackingInfo.shipsToUnload shouldHaveSize 0
        }

        // let's move the player out. They should be 100.1 blocks from the edge of the chunk now
        player.position.set(116.1, 0.0, 0.0)

        // ship should unload
        run {
            val (watchTasks, unwatchTasks) = tracker.generateChunkWatchTasksAndUpdatePlayers(
                players, players, ships, listOf()
            )

            watchTasks shouldHaveSize 0
            unwatchTasks shouldHaveSize 1

            val trackingInfo = tracker.applyTasksAndGenerateTrackingInfo(watchTasks, unwatchTasks)
            trackingInfo.playersToShipsNewlyWatchingMap shouldHaveSize 0
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 0
            trackingInfo.playersToShipsNoLongerWatchingMap shouldHaveSize 1
            trackingInfo.shipsToLoad shouldHaveSize 0
            trackingInfo.shipsToUnload shouldHaveSize 1
        }

    }
})

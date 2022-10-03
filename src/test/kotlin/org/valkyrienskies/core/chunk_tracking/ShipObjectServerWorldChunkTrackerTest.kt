package org.valkyrienskies.core.chunk_tracking

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.valkyrienskies.core.config.VSCoreConfig
import org.valkyrienskies.test_utils.VSBlankUtils
import org.valkyrienskies.test_utils.fakes.FakePlayer

class ShipObjectServerWorldChunkTrackerTest : StringSpec({

    "tracks single player" {
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

    "tracks player leaving the dimension and returning" {
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

        // nothing changes
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

        // move the player to a new dimension
        player.dimension = "fake_dimension2"

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

        // nothing changes
        run {
            val (watchTasks, unwatchTasks) = tracker.generateChunkWatchTasksAndUpdatePlayers(
                players, players, ships, listOf()
            )

            watchTasks shouldHaveSize 0
            unwatchTasks shouldHaveSize 0

            val trackingInfo = tracker.applyTasksAndGenerateTrackingInfo(watchTasks, unwatchTasks)

            trackingInfo.playersToShipsNewlyWatchingMap shouldHaveSize 0
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 0
            trackingInfo.playersToShipsNoLongerWatchingMap shouldHaveSize 0
            trackingInfo.shipsToLoad shouldHaveSize 0
            trackingInfo.shipsToUnload shouldHaveSize 0
        }

        // move player back to their original dimension
        player.dimension = "fake_dimension"

        // ship should load
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

        // nothing changes
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
    }

    "tracks multiple players and multiple chunks" {
        val dimension = "fake_dimension"
        val ship = VSBlankUtils.blankShipData(chunkClaimDimension = dimension)
        ship.shipActiveChunksSet.addChunkPos(0, 0) // 0 to 16
        ship.shipActiveChunksSet.addChunkPos(4, 0) // 64 to 80
        val ships = listOf(ship)

        val player1 = FakePlayer(dimension = dimension)
        val player2 = FakePlayer(dimension = dimension)
        val players = setOf(player1, player2)

        val config = VSCoreConfig.Server()
        config.shipLoadDistance = 50.0
        config.shipUnloadDistance = 100.0

        val tracker = ShipObjectServerWorldChunkTracker(config)

        // Both players at the same coordinate should watch the first chunk
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

            trackingInfo.playersToShipsNewlyWatchingMap shouldHaveSize 2
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 2
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
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 2
            trackingInfo.playersToShipsNoLongerWatchingMap shouldHaveSize 0
            trackingInfo.shipsToLoad shouldHaveSize 0
            trackingInfo.shipsToUnload shouldHaveSize 0
        }

        // let's move the first player out. They should be 13 blocks from the edge of the second chunk (and 51 from the edge of the first)
        // they should start tracking the second chunk
        player1.position.set(67.0, 0.0, 0.0)

        // the second chunk should get loaded
        run {
            val (watchTasks, unwatchTasks) = tracker.generateChunkWatchTasksAndUpdatePlayers(
                players, players, ships, listOf()
            )

            watchTasks shouldHaveSize 1
            unwatchTasks shouldHaveSize 0

            val trackingInfo = tracker.applyTasksAndGenerateTrackingInfo(watchTasks, unwatchTasks)

            trackingInfo.playersToShipsNewlyWatchingMap shouldHaveSize 0
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 2
            trackingInfo.playersToShipsNoLongerWatchingMap shouldHaveSize 0
            trackingInfo.shipsToLoad shouldHaveSize 0
            trackingInfo.shipsToUnload shouldHaveSize 0
        }

        // let's move player1 out. They should be 100.1 blocks from the edge of the first chunk now
        player1.position.set(116.1, 0.0, 0.0)

        // ship should NOT unload,
        // but player1 should stop tracking one of the chunks
        run {
            val (watchTasks, unwatchTasks) = tracker.generateChunkWatchTasksAndUpdatePlayers(
                players, players, ships, listOf()
            )

            watchTasks shouldHaveSize 0
            unwatchTasks shouldHaveSize 1

            val trackingInfo = tracker.applyTasksAndGenerateTrackingInfo(watchTasks, unwatchTasks)
            trackingInfo.playersToShipsNewlyWatchingMap shouldHaveSize 0
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 2
            trackingInfo.playersToShipsNoLongerWatchingMap shouldHaveSize 0
            trackingInfo.shipsToLoad shouldHaveSize 0
            trackingInfo.shipsToUnload shouldHaveSize 0
        }

        // move player1 out further. They should no longer track any chunks
        player1.position.set(180.1, 0.0, 0.0)

        // ship still shouldn't unload (player2 keeping it loaded)
        run {
            val (watchTasks, unwatchTasks) = tracker.generateChunkWatchTasksAndUpdatePlayers(
                players, players, ships, listOf()
            )

            watchTasks shouldHaveSize 0
            unwatchTasks shouldHaveSize 1

            val trackingInfo = tracker.applyTasksAndGenerateTrackingInfo(watchTasks, unwatchTasks)
            trackingInfo.playersToShipsNewlyWatchingMap shouldHaveSize 0
            trackingInfo.playersToShipsWatchingMap shouldHaveSize 1
            trackingInfo.playersToShipsNoLongerWatchingMap shouldHaveSize 1
            trackingInfo.shipsToLoad shouldHaveSize 0
            trackingInfo.shipsToUnload shouldHaveSize 0
        }
    }
})

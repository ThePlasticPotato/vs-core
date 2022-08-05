package org.valkyrienskies.core.game.ships

import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.game.ChunkAllocator
import org.valkyrienskies.core.util.InternalInject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.BINARY

@Retention(BINARY)
@Qualifier
annotation class AllShips

@Module
class SerializedShipDataModule(
    @get:Provides @get:Singleton @get:AllShips val queryableShipData: MutableQueryableShipDataServer,
    @get:Provides @get:Singleton @get:InternalInject val chunkAllocator: ChunkAllocator
)

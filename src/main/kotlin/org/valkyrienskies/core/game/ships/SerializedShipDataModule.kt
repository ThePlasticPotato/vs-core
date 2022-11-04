package org.valkyrienskies.core.game.ships

import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.game.ChunkAllocator
import org.valkyrienskies.core.util.InternalInject
import org.valkyrienskies.core.util.WorldScoped
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

@Retention(BINARY)
@Qualifier
annotation class AllShips

@Module
class SerializedShipDataModule(
    @get:Provides @get:WorldScoped @get:AllShips val queryableShipData: MutableQueryableShipDataServer,
    @get:Provides @get:WorldScoped @get:InternalInject val chunkAllocator: ChunkAllocator
) {
    companion object {
        fun createEmpty() = SerializedShipDataModule(
            queryableShipData = QueryableShipDataImpl(),
            chunkAllocator = ChunkAllocator.create()
        )
    }
}

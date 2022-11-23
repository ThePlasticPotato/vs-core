package org.valkyrienskies.core.game.ships.modules

import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.game.ChunkAllocator
import org.valkyrienskies.core.game.ChunkAllocatorProvider
import org.valkyrienskies.core.game.SingletonChunkAllocatorProviderImpl

@Module
class ClientShipWorldModule {

    @Module
    interface Declarations {

        companion object {
            @Provides
            fun chunkAllocatorProvider(): ChunkAllocatorProvider =
                SingletonChunkAllocatorProviderImpl(ChunkAllocator.create())
        }
    }
}
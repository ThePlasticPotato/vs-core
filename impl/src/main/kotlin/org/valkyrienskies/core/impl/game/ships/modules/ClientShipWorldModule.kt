package org.valkyrienskies.core.impl.game.ships.modules

import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.impl.game.ChunkAllocator
import org.valkyrienskies.core.impl.game.ChunkAllocatorProvider
import org.valkyrienskies.core.impl.game.SingletonChunkAllocatorProviderImpl

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

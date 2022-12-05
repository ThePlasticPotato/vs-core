package org.valkyrienskies.core.impl.game.ships.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ChunkAllocator
import org.valkyrienskies.core.impl.game.ChunkAllocatorProvider
import org.valkyrienskies.core.impl.game.DimensionInfo
import org.valkyrienskies.core.impl.game.SingletonChunkAllocatorProviderImpl
import org.valkyrienskies.core.impl.game.ships.MutableQueryableShipDataServer
import org.valkyrienskies.core.impl.game.ships.QueryableShipDataImpl
import org.valkyrienskies.core.impl.util.WorldScoped
import javax.inject.Named
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

@Retention(BINARY)
@Qualifier
annotation class AllShips

/**
 * Creates the necessary dependency graph for the VSPipeline subcomponent, aka the stuff that each individual world
 * (save file, not dimension) needs. [allShips] are the existing ships in the world, and
 * [chunkAllocator] is the existing chunk allocator for the world.
 */
@Module
class ShipWorldModule(
    @get:Provides @get:WorldScoped @get:AllShips
    val allShips: MutableQueryableShipDataServer,

    @get:Provides @get:WorldScoped @get:Named("primary")
    val chunkAllocator: ChunkAllocator
) {

    @get:Provides
    @get:WorldScoped
    @get:Named("mutableDimensionInfo")
    val mutableDimensionInfo = mutableMapOf<DimensionId, DimensionInfo>()

    @get:Provides
    @get:WorldScoped
    @get:Named("dimensionInfo")
    val dimensionInfo: Map<DimensionId, DimensionInfo> get() = mutableDimensionInfo

    @Module
    interface Declarations {
        @Binds
        fun chunkAllocatorProvider(impl: SingletonChunkAllocatorProviderImpl): ChunkAllocatorProvider
    }

    companion object {
        fun createEmpty() = ShipWorldModule(
            allShips = QueryableShipDataImpl(),
            chunkAllocator = ChunkAllocator.create()
        )
    }
}

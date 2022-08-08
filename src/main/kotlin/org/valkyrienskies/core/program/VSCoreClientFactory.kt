package org.valkyrienskies.core.program

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [VSCoreClientModule::class])
interface VSCoreClientFactory {
    fun client(): VSCoreClient
}

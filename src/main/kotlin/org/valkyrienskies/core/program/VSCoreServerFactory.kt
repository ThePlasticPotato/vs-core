package org.valkyrienskies.core.program

import dagger.Component
import javax.inject.Singleton

@Component(
    modules = [VSCoreModule::class]
)
@Singleton
interface VSCoreServerFactory {
    fun server(): VSCoreServer
}

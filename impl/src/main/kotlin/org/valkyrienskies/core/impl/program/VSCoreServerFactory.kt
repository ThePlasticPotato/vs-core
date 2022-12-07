package org.valkyrienskies.core.impl.program

import dagger.Component
import javax.inject.Singleton

@Component(
    modules = [VSCoreModule::class]
)
@Singleton
interface VSCoreServerFactory {
    fun server(): VSCoreServerImpl
}

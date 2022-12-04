package org.valkyrienskies.core.impl.program

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [VSCoreClientModule::class])
interface VSCoreClientFactory {
    fun client(): VSCoreClientImpl
}

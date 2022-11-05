package org.valkyrienskies.test_utils.fakes

import org.valkyrienskies.core.program.DaggerVSCoreClientFactory
import org.valkyrienskies.core.program.DaggerVSCoreServerFactory
import org.valkyrienskies.core.program.VSCoreClient
import org.valkyrienskies.core.program.VSCoreModule
import org.valkyrienskies.core.program.VSCoreServer

object FakeVSCoreFactory {

    fun fakeVsCoreModule() = VSCoreModule(FakeAbstractCoreHooks()) {}

    fun fakeVsCoreServer(): VSCoreServer {
        return DaggerVSCoreServerFactory.builder().vSCoreModule(fakeVsCoreModule()).build().server()
    }

    fun fakeVsCoreClient(): VSCoreClient {
        return DaggerVSCoreClientFactory.builder().vSCoreModule(fakeVsCoreModule()).build().client()
    }
}

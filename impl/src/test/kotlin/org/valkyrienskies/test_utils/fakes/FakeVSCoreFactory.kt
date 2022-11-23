package org.valkyrienskies.test_utils.fakes

import org.valkyrienskies.core.program.*

object FakeVSCoreFactory {

    fun fakeVsCoreModule() = VSCoreModule(FakeAbstractCoreHooks()) {}

    fun fakeVsCoreServer(): VSCoreServerImpl {
        return DaggerVSCoreServerFactory.builder().vSCoreModule(fakeVsCoreModule()).build().server()
    }

    fun fakeVsCoreClient(): VSCoreClientImpl {
        return DaggerVSCoreClientFactory.builder().vSCoreModule(fakeVsCoreModule()).build().client()
    }
}

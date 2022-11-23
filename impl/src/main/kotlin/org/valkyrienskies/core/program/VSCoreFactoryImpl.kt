package org.valkyrienskies.core.program

import org.valkyrienskies.core.api.VSCoreClient
import org.valkyrienskies.core.api.VSCoreFactory
import org.valkyrienskies.core.api.VSCoreServer
import org.valkyrienskies.core.api.hooks.CoreHooksOut
import org.valkyrienskies.core.networking.VSNetworkingConfigurator

class VSCoreFactoryImpl : VSCoreFactory {
    override fun newVsCoreClient(hooks: CoreHooksOut): VSCoreClient {
        return DaggerVSCoreClientFactory.builder().vSCoreModule(makeModule(hooks)).build().client();
    }

    override fun newVsCoreServer(hooks: CoreHooksOut): VSCoreServer {
        return DaggerVSCoreServerFactory.builder().vSCoreModule(makeModule(hooks)).build().server()
    }

    private fun makeModule(hooks: CoreHooksOut): VSCoreModule {
        val configurator = VSNetworkingConfigurator {
            it.rawSendToServer = hooks::sendToServer
            it.rawSendToClient = hooks::sendToClient
        }

        return VSCoreModule(hooks, configurator)
    }
}
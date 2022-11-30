package org.valkyrienskies.core.program

import org.valkyrienskies.core.api.VSCoreClient
import org.valkyrienskies.core.api.VSCoreFactory
import org.valkyrienskies.core.api.VSCoreServer
import org.valkyrienskies.core.api.hooks.CoreHooksOut
import org.valkyrienskies.core.hooks.CoreHooks
import org.valkyrienskies.core.networking.VSNetworkingConfigurator

class VSCoreFactoryImpl : VSCoreFactory {
    override fun newVsCoreClient(hooks: CoreHooksOut): VSCoreClient {
        val client = DaggerVSCoreClientFactory.builder().vSCoreModule(makeModule(hooks)).build().client()

        CoreHooks = client.hooks

        return client
    }

    override fun newVsCoreServer(hooks: CoreHooksOut): VSCoreServer {
        val server = DaggerVSCoreServerFactory.builder().vSCoreModule(makeModule(hooks)).build().server()

        CoreHooks = server.hooks

        return server
    }

    private fun makeModule(hooks: CoreHooksOut): VSCoreModule {
        val configurator = VSNetworkingConfigurator {
            it.rawSendToServer = hooks::sendToServer
            it.rawSendToClient = hooks::sendToClient
        }

        return VSCoreModule(hooks, configurator)
    }
}

package org.valkyrienskies.core.impl.program

import org.valkyrienskies.core.apigame.VSCoreClient
import org.valkyrienskies.core.apigame.VSCoreFactory
import org.valkyrienskies.core.apigame.VSCoreServer
import org.valkyrienskies.core.apigame.hooks.CoreHooksOut
import org.valkyrienskies.core.impl.hooks.CoreHooks
import org.valkyrienskies.core.impl.networking.VSNetworkingConfigurator

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

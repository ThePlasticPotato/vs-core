package org.valkyrienskies.core.apigame

import org.valkyrienskies.core.apigame.hooks.CoreHooksOut

interface VSCoreFactory {

    fun newVsCoreClient(hooks: CoreHooksOut): VSCoreClient

    fun newVsCoreServer(hooks: CoreHooksOut): VSCoreServer

    companion object {

        @JvmStatic
        val instance: VSCoreFactory = findInstance()

        private fun findInstance(): VSCoreFactory {
            // yea, froge breaks ServiceLoader because jpms. cronge
            return Class.forName("org.valkyrienskies.core.impl.program.VSCoreFactoryImpl")
                .getDeclaredConstructor().newInstance() as VSCoreFactory
        }
    }
}

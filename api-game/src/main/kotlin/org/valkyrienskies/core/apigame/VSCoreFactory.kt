package org.valkyrienskies.core.apigame


import org.valkyrienskies.core.apigame.hooks.CoreHooksOut
import java.util.*

interface VSCoreFactory {

    fun newVsCoreClient(hooks: CoreHooksOut): VSCoreClient

    fun newVsCoreServer(hooks: CoreHooksOut): VSCoreServer

    companion object {

        @JvmStatic
        val instance: VSCoreFactory = findInstance()

        private fun findInstance(): VSCoreFactory {
            val instances = ServiceLoader.load(VSCoreFactory::class.java).toList()
            require(instances.size == 1) {
                "Found ${instances.size} instances of VSCoreGameFactory, required exactly one!"
            }
            return instances.first()
        }
    }
}

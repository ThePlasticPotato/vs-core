package org.valkyrienskies.core.api

import java.util.*

interface VSCoreGameFactory {

    fun newVsCoreGame(): VSCoreGame

    companion object {

        @JvmStatic
        val instance: VSCoreGameFactory = findInstance()

        private fun findInstance(): VSCoreGameFactory {
            val instances = ServiceLoader.load(VSCoreGameFactory::class.java).toList()
            require(instances.size == 1) {
                "Found ${instances.size} instances of VSCoreGameFactory, required exactly one!"
            }
            return instances.first()
        }
    }
}

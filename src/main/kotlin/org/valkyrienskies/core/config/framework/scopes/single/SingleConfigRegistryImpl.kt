package org.valkyrienskies.core.config.framework.scopes.single

import com.fasterxml.jackson.databind.node.ObjectNode
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.checkerframework.checker.units.qual.C
import org.valkyrienskies.core.config.framework.ConfigInstance
import org.valkyrienskies.core.config.framework.ConfigInstanceImpl
import org.valkyrienskies.core.config.framework.ConfigType
import org.valkyrienskies.core.config.framework.ConfigTypeRegistry
import org.valkyrienskies.core.config.framework.RawConfigInstance
import org.valkyrienskies.core.config.framework.synchronize.ConfigSynchronizer
import java.util.concurrent.ConcurrentHashMap

class SingleConfigRegistryImpl @AssistedInject constructor(
    private val typeRegistry: ConfigTypeRegistry,
    @Assisted private val synchronizers: List<ConfigSynchronizer<*>>
) : SingleConfigRegistry {

    @AssistedFactory
    interface Factory {
        fun newSingleConfigRegistry(
            synchronizers: List<ConfigSynchronizer<*>>
        ): SingleConfigRegistryImpl
    }

    private val instances = ConcurrentHashMap<Class<*>, ConfigInstance<*, *>>()

    override fun <C : SingleScopedConfig> register(id: String, clazz: Class<C>): ConfigType<C, Unit> {
        val type = typeRegistry.registerConfigType(id, clazz)

        // we can use the same id for type and instance, since there's only one instance per type :)
        val instance = ConfigInstanceImpl(type, type.mapper, id)
        instances[clazz] = instance

        synchronizers.forEach { it.apply(instance) }

        return type
    }

    override fun <C : SingleScopedConfig> getInstance(configClass: Class<C>): ConfigInstance<C, Unit> {
        @Suppress("UNCHECKED_CAST")
        return requireNotNull(instances[configClass] as ConfigInstance<C, Unit>?) {
            "Could not find registered singleton config instance for $configClass"
        }
    }

    fun getInstance(configClass: Class<*>): RawConfigInstance<*> {
        return requireNotNull(instances[configClass]) {
            "Could not find registered singleton config instance for $configClass"
        }
    }

    override fun <C : SingleScopedConfig> getScope(configClass: Class<C>): SingleConfig<C> {
        val instance = getInstance(configClass)

        val ctx = Unit

        return object : SingleConfig<C> {
            override fun get(): C = instance.value
            override fun set(value: ObjectNode) = instance.attemptUpdate(value, ctx)
            override fun set(value: C) = instance.attemptUpdate(value, ctx)
        }
    }
}

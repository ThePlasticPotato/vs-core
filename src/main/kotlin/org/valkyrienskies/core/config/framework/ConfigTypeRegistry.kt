package org.valkyrienskies.core.config.framework

import org.valkyrienskies.core.config.framework.ConfigType.Factory
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigTypeRegistry @Inject internal constructor(
    private val configTypeFactory: Factory,
) {

    private val types = ConcurrentHashMap<Class<*>, ConfigType<*, *>>()
    private val registeredNames = mutableSetOf<String>()

    @Suppress("UNCHECKED_CAST")
    fun <C : ScopedConfig<X>, X> getConfigType(clazz: Class<C>): ConfigType<C, X> =
        requireNotNull(types[clazz] as ConfigType<C, X>?) { "Config class $clazz not registered" }

    fun <C : ScopedConfig<X>, X> registerConfigType(id: String, configClass: Class<C>): ConfigType<C, X> {
        val split = id.split(":")

        require(split.size == 2) {
            "Must have name and namespace separated by one colon (:)"
        }

        val (namespace, name) = split

        require(!types.contains(configClass)) {
            "This config class was already registered: $configClass"
        }
        require(registeredNames.add(namespace + name)) {
            "A config with the same name and namespace was registered: $namespace:$name "
        }

        val type = configTypeFactory.create(namespace, name, configClass)
        types[configClass] = type
        return type
    }
}

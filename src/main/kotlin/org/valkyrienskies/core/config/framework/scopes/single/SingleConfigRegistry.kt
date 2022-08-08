package org.valkyrienskies.core.config.framework.scopes.single

import org.valkyrienskies.core.config.framework.ConfigInstance
import org.valkyrienskies.core.config.framework.ConfigType

interface SingleConfigRegistry {
    fun <C : SingleScopedConfig> register(id: String, clazz: Class<C>): ConfigType<C, Unit>
    fun <C : SingleScopedConfig> getInstance(configClass: Class<C>): ConfigInstance<C, Unit>
    fun <C : SingleScopedConfig> getScope(configClass: Class<C>): SingleConfig<C>

    fun <C : SingleScopedConfig> registerAndGet(id: String, clazz: Class<C>): SingleConfig<C> {
        register(id, clazz)
        return getScope(clazz)
    }
}

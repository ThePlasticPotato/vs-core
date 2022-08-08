package org.valkyrienskies.core.config.framework.synchronize

import org.valkyrienskies.core.config.framework.ConfigContext
import org.valkyrienskies.core.config.framework.RawConfigInstance

interface ConfigSynchronizer<X : ConfigContext> {

    fun startSynchronizing() {}

    fun onUpdate(instance: RawConfigInstance<X>)
}

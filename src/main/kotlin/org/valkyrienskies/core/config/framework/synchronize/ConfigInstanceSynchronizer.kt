package org.valkyrienskies.core.config.framework.synchronize

import org.valkyrienskies.core.config.framework.RawConfigInstance

interface ConfigInstanceSynchronizer<X> {

    fun afterUpdate(config: RawConfigInstance<*>, ctx: X)
}

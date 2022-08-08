package org.valkyrienskies.core.config.framework

import org.checkerframework.checker.units.qual.C

interface ConfigInstance<out C : ScopedConfig<X>, X> : RawConfigInstance<X> {
    val type: ConfigType<C, X>
    override val value: C
}

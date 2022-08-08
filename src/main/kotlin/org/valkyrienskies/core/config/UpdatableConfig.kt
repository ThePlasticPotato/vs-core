package org.valkyrienskies.core.config

import org.valkyrienskies.core.config.framework.scopes.single.SingleScopedConfig
import kotlin.DeprecationLevel.ERROR

@Deprecated(
    message = "Use SingleScopedConfig", level = ERROR,
    replaceWith = ReplaceWith(
        "SingleScopedConfig",
        "org.valkyrienskies.core.config.framework.SingleScopedConfig"
    )
)
typealias UpdatableConfig = SingleScopedConfig


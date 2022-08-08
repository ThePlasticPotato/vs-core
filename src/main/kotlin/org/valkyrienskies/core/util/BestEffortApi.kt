package org.valkyrienskies.core.util

import kotlin.RequiresOptIn.Level.WARNING

@RequiresOptIn(
    level = WARNING,
    message = "This VS Core API is public, but backwards compatibility is \"best effort\" and not guaranteed, " +
        "even across minor version changes"
)
annotation class BestEffortApi

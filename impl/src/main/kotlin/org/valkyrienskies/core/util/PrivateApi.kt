package org.valkyrienskies.core.util

import kotlin.RequiresOptIn.Level.ERROR

/**
 * Marks a part of the API as private and part of VS Core only. Useful for marking parts of interface as internal,
 * which is otherwise not possible.
 *
 * Use with [JvmSynthetic] to hide the API from java.
 *
 * Opt-in using kotlinOptions (see build.gradle.kts)
 */
@RequiresOptIn(
    level = ERROR,
    message = "Private VS Core API, do not use"
)
internal annotation class PrivateApi

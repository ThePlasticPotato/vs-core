package org.valkyrienskies.core.config.framework.exceptions

open class ConfigException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

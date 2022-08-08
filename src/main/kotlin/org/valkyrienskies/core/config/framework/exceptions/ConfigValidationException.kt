package org.valkyrienskies.core.config.framework.exceptions

class ConfigValidationException(
    message: String,
    cause: Throwable? = null
) : ConfigException(message, cause)

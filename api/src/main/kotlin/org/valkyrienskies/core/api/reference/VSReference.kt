package org.valkyrienskies.core.api.reference

import java.util.*

interface VSReference<out T : Any> {

    val type: Class<out T>

    fun get(): T?

    fun getOptional(): Optional<out T> = Optional.ofNullable(get())

    fun getOrThrow(): T

}
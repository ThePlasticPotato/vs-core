package org.valkyrienskies.core.api.reference

import java.util.*

interface VSRef<out T : Any> {

    val type: Class<out T>

    fun get(): T?

    fun getOptional(): Optional<out T> = Optional.ofNullable(get())

    fun getOrThrow(): T

}
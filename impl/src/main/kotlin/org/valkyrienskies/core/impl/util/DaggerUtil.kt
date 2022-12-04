package org.valkyrienskies.core.impl.util

import kotlin.reflect.KProperty

operator fun <T> dagger.Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T = this.get()

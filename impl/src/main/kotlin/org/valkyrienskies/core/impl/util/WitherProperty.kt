package org.valkyrienskies.core.impl.util

import kotlin.reflect.KMutableProperty0

class WitherProperty<B, T>(val backing: KMutableProperty0<B>, val wither: B.(T) -> B, val getter: B.() -> T)
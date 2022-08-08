package org.valkyrienskies.core.util

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

@Retention(BINARY)
@Qualifier
internal annotation class InternalInject()

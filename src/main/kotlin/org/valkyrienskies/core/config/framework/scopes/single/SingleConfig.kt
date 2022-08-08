package org.valkyrienskies.core.config.framework.scopes.single

import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface SingleConfig<C : Any> : ReadWriteProperty<Any?, C> {

    fun get(): C
    fun set(value: C)
    fun set(value: ObjectNode)

    override fun getValue(thisRef: Any?, property: KProperty<*>): C = get()
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: C) = set(value)
}

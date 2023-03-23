package org.valkyrienskies.core.impl.entities

import org.valkyrienskies.core.api.util.HasId
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ObservableProperty<T>(var value: T) : ReadWriteProperty<HasId, T> {

    private val changeListeners = ConcurrentHashMap.newKeySet<PropertyChangeListener<T>>()

    fun addListener(listener: PropertyChangeListener<T>) {
        changeListeners.add(listener)
    }

    fun removeListener(listener: PropertyChangeListener<T>) {
        changeListeners.remove(listener)
    }

    override fun getValue(thisRef: HasId, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: HasId, property: KProperty<*>, value: T) {
        setValueAndNotify(thisRef, value)
    }

    fun notifyMutation(thisRef: HasId) {
        changeListeners.forEach { it.onChanged(this, thisRef, value, value) }
    }

    fun setValueAndNotify(thisRef: HasId, value: T) {
        changeListeners.forEach { it.onChanged(this, thisRef, this.value, value) }
        this.value = value
    }

}
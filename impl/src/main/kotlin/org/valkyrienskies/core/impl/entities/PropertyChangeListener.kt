package org.valkyrienskies.core.impl.entities

import org.valkyrienskies.core.api.util.HasId

fun interface PropertyChangeListener<T> {
    fun onChanged(property: ObservableProperty<T>, entity: HasId, oldValue: T, newValue: T)
}


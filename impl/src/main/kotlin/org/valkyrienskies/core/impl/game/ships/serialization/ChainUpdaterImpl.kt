package org.valkyrienskies.core.impl.game.ships.serialization

class ChainUpdaterImpl<T>(
    val updateTo: Class<T>,
    vararg mappings: Pair<Class<*>, DtoUpdater<*, *>>
) : ChainUpdater<T> {

    private val mappings = mappings.toMap()

    @Suppress("UNCHECKED_CAST")
    override fun updateToLatest(value: Any): T {
        var current = value

        while (!updateTo.isInstance(current)) {
            val currentType = current::class.java
            val updater = mappings[currentType] as DtoUpdater<Any, Any>?
                ?: throw IllegalArgumentException("Could not find updater for type $currentType")

            current = updater.update(current)
        }

        return current as T
    }
}

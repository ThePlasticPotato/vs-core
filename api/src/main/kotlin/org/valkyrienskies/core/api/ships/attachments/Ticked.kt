package org.valkyrienskies.core.api.ships.attachments

/**
 * Defines a class that wants to get ticked (in the server thread)
 */
interface Ticked {

    fun tick()
}

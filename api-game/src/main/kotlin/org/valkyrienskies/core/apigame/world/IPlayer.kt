package org.valkyrienskies.core.apigame.world

import org.joml.Vector3d
import org.valkyrienskies.core.api.world.properties.DimensionId
import java.util.UUID

/**
 * An interface that represents players.
 */
interface IPlayer {
    /**
     * Sets [dest] to be the current position of this [IPlayer], and then returns dest.
     */
    fun getPosition(dest: Vector3d): Vector3d

    val dimension: DimensionId

    val uuid: UUID

    val isAdmin: Boolean

    val canModifyServerConfig: Boolean get() = isAdmin
}

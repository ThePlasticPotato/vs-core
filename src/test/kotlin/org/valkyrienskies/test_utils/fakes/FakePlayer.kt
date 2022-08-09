package org.valkyrienskies.test_utils.fakes

import org.joml.Vector3d
import org.valkyrienskies.core.game.DimensionId
import org.valkyrienskies.core.game.IPlayer
import java.util.UUID

class FakePlayer(
    var position: Vector3d = Vector3d(),
    override var dimension: DimensionId = "fake_dimension",
    override var uuid: UUID = UUID.randomUUID(),
    override var isAdmin: Boolean = false
) : IPlayer {
    override fun getPosition(dest: Vector3d): Vector3d {
        return dest.set(position)
    }
}

package org.valkyrienskies.core.impl.game.ships.serialization.shipinertia

import dagger.Reusable
import org.joml.Matrix3d
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.properties.ShipInertiaData
import org.valkyrienskies.core.impl.game.ships.ShipInertiaDataImpl
import org.valkyrienskies.core.impl.game.ships.serialization.shipinertia.dto.ShipInertiaDataV0
import javax.inject.Inject

@Reusable
class ShipInertiaConverter @Inject constructor() {

    fun convertToModel(data: ShipInertiaDataV0): ShipInertiaDataImpl =
        ShipInertiaDataImpl(data.centerOfMassInShipSpace, data.shipMass, data.momentOfInertiaTensor)

    fun convertToDto(model: ShipInertiaData): ShipInertiaDataV0 =
        ShipInertiaDataV0(Vector3d(model.centerOfMassInShip), model.mass, Matrix3d(model.momentOfInertiaTensorToSave))
}

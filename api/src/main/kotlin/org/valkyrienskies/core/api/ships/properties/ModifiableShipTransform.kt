package org.valkyrienskies.core.api.ships.properties

import org.joml.Quaterniondc
import org.joml.Vector3dc
import org.valkyrienskies.core.api.VSBeta

@VSBeta
interface ModifiableShipTransform : ShipTransform {
    override var positionInWorld: Vector3dc
    override var rotation: Quaterniondc
    override var scaling: Double
}

package org.valkyrienskies.core.api.ships

import org.joml.Matrix4dc

interface ContraptionWingProvider {
    var wingGroupId: WingGroupId
    fun computeContraptionWingTransform(): Matrix4dc
}

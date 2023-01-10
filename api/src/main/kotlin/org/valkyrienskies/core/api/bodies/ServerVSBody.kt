package org.valkyrienskies.core.api.bodies

import org.valkyrienskies.core.api.bodies.properties.BodyInertiaData

interface ServerVSBody : VSBody {

    val inertia: BodyInertiaData

}
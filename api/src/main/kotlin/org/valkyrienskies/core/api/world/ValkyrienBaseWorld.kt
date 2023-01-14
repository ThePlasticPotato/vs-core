package org.valkyrienskies.core.api.world

import org.valkyrienskies.core.api.attachment.AttachmentHolder
import org.valkyrienskies.core.api.bodies.BaseVSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.reference.VSRef

interface ValkyrienBaseWorld : AttachmentHolder {

    fun getBody(id: BodyId): BaseVSBody?

    fun getBodyReference(id: BodyId): VSRef<BaseVSBody>

}
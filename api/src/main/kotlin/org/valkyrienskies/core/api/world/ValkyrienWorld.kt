package org.valkyrienskies.core.api.world

import org.valkyrienskies.core.api.attachment.AttachmentHolder
import org.valkyrienskies.core.api.bodies.VSBody
import org.valkyrienskies.core.api.bodies.properties.BodyId
import org.valkyrienskies.core.api.bodies.reference.VSBodyReference

interface ValkyrienWorld {

    val attachments: AttachmentHolder

    fun getBody(id: BodyId): VSBody?

    fun getBodyReference(id: BodyId): VSBodyReference

}
package org.valkyrienskies.core.impl.attachment

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.valkyrienskies.core.impl.attachment.meta.AttachmentMetaRegistry
import javax.inject.Inject
import javax.inject.Named

class AttachmentDeserializer @Inject constructor(
    @Named("dto") val mapper: ObjectMapper,
    val meta: AttachmentMetaRegistry
) {

    fun deserialize(key: String, value: JsonNode) {
        value.binaryValue()
        value.isBinary
    }

}
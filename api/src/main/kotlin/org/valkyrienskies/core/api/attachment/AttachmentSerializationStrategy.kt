package org.valkyrienskies.core.api.attachment

import com.fasterxml.jackson.databind.JsonNode
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
sealed interface AttachmentSerializationStrategy {
    interface Binary : AttachmentSerializationStrategy {
        fun serialize(obj: Any): ByteArray

        fun deserialize(data: ByteArray): Any
    }

    interface Json : AttachmentSerializationStrategy {
        fun serialize(obj: Any): JsonNode

        fun deserialize(data: JsonNode): Any
    }

    object Transient : AttachmentSerializationStrategy

    object None : AttachmentSerializationStrategy
}
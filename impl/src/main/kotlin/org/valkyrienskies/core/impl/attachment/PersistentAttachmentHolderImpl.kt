package org.valkyrienskies.core.impl.attachment

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.valkyrienskies.core.api.attachment.AttachmentHolder
import org.valkyrienskies.core.api.attachment.AttachmentSerializationStrategy
import org.valkyrienskies.core.impl.attachment.meta.AttachmentMetaRegistry
import java.util.function.Supplier
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class PersistentAttachmentHolderImpl private constructor(
    private val meta: AttachmentMetaRegistry,
    val attachments: MutableMap<Class<*>, Any>
) : AttachmentHolder {
    override fun <T : Any> getAttachment(clazz: Class<T>): T? {
        return attachments[clazz] as T?
    }

    override fun <T : Any> getOrPutAttachment(clazz: Class<T>, supplier: Supplier<T>): T {
        return attachments.computeIfAbsent(clazz) { supplier.get() } as T
    }

    override fun <T : Any> setAttachment(value: T, clazz: Class<T>): T? {
        return attachments.put(clazz, value) as T?
    }

    override fun <T : Any> removeAttachment(clazz: Class<T>): T? {
        return attachments.remove(clazz) as T?
    }

    fun serialize(): ObjectNode {
        val json = JsonNodeFactory.instance.objectNode()
        for ((clazz, value) in attachments) {
            val attachment = meta.getAttachment(clazz)
            val strategy = meta.getSerializationStrategy(attachment)
            val ser = strategy.serialize(value)
            if (ser != null) {
                json.replace(attachment.key, ser)
            }
        }
        return json
    }

    class Factory @Inject constructor(
        private val meta: AttachmentMetaRegistry,
    ) {
        fun create(json: ObjectNode): PersistentAttachmentHolderImpl {
            val map = mutableMapOf<Class<*>, Any>()
            for ((key, value) in json.fields()) {
                val attachment = meta.getAttachment(key)
                val strategy = meta.getSerializationStrategy(attachment)
                val deser = requireNotNull(strategy.deserialize(value)) {
                    "Could not deserialize attachment with key: $key, serialization strategy is NONE"
                }

                val latest = meta.updateToLatest(deser).attachment
                map[latest::class.java] = latest
            }

            return PersistentAttachmentHolderImpl(meta, map)
        }
    }
}

private fun AttachmentSerializationStrategy.serialize(value: Any): JsonNode? {
    return when(this) {
        is AttachmentSerializationStrategy.Binary -> BinaryNode(serialize(value))
        is AttachmentSerializationStrategy.Json -> serialize(value)
        is AttachmentSerializationStrategy.Transient -> null
        is AttachmentSerializationStrategy.None ->
            throw IllegalArgumentException("Attempted to serialize attachment not meant to be serialized: $value")
    }
}

private fun AttachmentSerializationStrategy.deserialize(value: JsonNode): Any? {
    return when (this) {
        is AttachmentSerializationStrategy.Binary -> deserialize(value.binaryValue())
        is AttachmentSerializationStrategy.Json -> deserialize(value)
        else -> null
    }
}
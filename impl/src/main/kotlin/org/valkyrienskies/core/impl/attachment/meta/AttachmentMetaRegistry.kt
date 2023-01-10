package org.valkyrienskies.core.impl.attachment.meta

import org.valkyrienskies.core.api.attachment.AttachmentSerializationStrategy
import org.valkyrienskies.core.api.attachment.AttachmentUpdater
import org.valkyrienskies.core.api.attachment.VSAttachment
import java.lang.reflect.Modifier
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentMetaRegistry @Inject constructor() {

    private val byClass = HashMap<Class<*>, VSAttachment>()
    private val byKey = HashMap<String, VSAttachment>()
    private val strategies = HashMap<String, AttachmentSerializationStrategy>()
    private val updaters = HashMap<Class<*>, AttachmentUpdater<*, *>>()

    init {
        registerStrategy("transient", AttachmentSerializationStrategy.Transient)
        registerStrategy("none", AttachmentSerializationStrategy.None)
    }

    fun registerStrategy(name: String, strategy: AttachmentSerializationStrategy) {
        strategies.putOrThrow(name, strategy) {
            "$name was already registered as a serialization strategy"
        }
    }

    fun <T : Any> registerUpdater(clazz: Class<T>, updater: AttachmentUpdater<T, *>) {
        updaters.putOrThrow(clazz, updater)
    }

    data class AttachmentAndKey(val attachment: Any, val key: String)

    @Suppress("UNCHECKED_CAST")
    fun updateToLatest(attachment: Any): AttachmentAndKey {
        var current = attachment
        var updater = updaters[current::class.java] as AttachmentUpdater<Any, *>?
        while (updater != null) {
            current = updater.update(current)
            updater = updaters[current::class.java] as AttachmentUpdater<Any, *>?
        }
        val key = getAttachment(current::class.java).key
        return AttachmentAndKey(current, key)
    }

    fun registerAttachment(clazz: Class<*>) {
        require(Modifier.isFinal(clazz.modifiers)) {
            "Attachment classes must be final!"
        }

        require(!clazz.isLocalClass && !clazz.isAnonymousClass) {
            "Attachment classes may not be local/anonymous!"
        }

        val meta = requireNotNull(clazz.getAnnotation(VSAttachment::class.java)) {
            "$clazz was registered as attachment, but is not annotated with @VSAttachment"
        }

        val key = if (meta.key == "") {
            clazz.name + ":1"
        } else {
            meta.key
        }

        byClass.putOrThrow(clazz, meta)
        byKey.putOrThrow(key, meta)
    }

    fun getSerializationStrategy(attachment: VSAttachment) =
        requireNotNull(strategies[attachment.serializationStrategy]) {
            "Couldn't find serialization strategy ${attachment.serializationStrategy}"
        }

    fun getAttachment(key: String)= requireNotNull(byKey[key]) {
        "Couldn't find attachment for key: $key"
    }
    fun getAttachment(clazz: Class<*>) = requireNotNull(byClass[clazz]) {
        "Couldn't find attachment for class: $clazz"
    }

}

private inline fun <K, V> MutableMap<K, V>.putOrThrow(
    k: K, v: V,
    messageSupplier: () -> String = { "$k was already in the map" }
) {
    require(putIfAbsent(k, v) == null, messageSupplier)
}
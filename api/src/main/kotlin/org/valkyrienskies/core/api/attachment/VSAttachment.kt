package org.valkyrienskies.core.api.attachment

/**
 * Use this to annotate a class that will be serialized as an attachment by VS
 *
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class VSAttachment(
    /**
     * Describes how this attachment should be serialized:
     *
     * - "jackson": Serializes using Jackson, including only public getters and creators by default
     * - "java": Serializes using default Java serialization. Must implement either Serializable or Externalizable
     * - "none": An exception will be thrown if the attachment is serialized
     * - "transient": The attachment will not be serialized
     */
    val serializationStrategy: String = "jackson",

    /**
     * Every attachment must have a unique key. If this is left blank, it will use the following suggested format:
     *
     * `full.qualified.name.of.YourAttachment:1`
     *
     * Appending a version number to the key is suggested, so you can easily update if need be.
     */
    val key: String = ""
)

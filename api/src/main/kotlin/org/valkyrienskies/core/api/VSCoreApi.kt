package org.valkyrienskies.core.api

import org.valkyrienskies.core.api.attachment.AttachmentSerializationStrategy

interface VSCoreApi {

    fun <T> registerAttachmentSerializationStrategy(name: String, strategy: Class<out AttachmentSerializationStrategy>)

    fun registerAttachments(vararg classes: Class<*>)

    fun registerAttachmentsInPackage(packageName: String)

}
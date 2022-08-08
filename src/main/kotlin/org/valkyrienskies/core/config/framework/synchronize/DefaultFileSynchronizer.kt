package org.valkyrienskies.core.config.framework.synchronize

import org.valkyrienskies.core.config.framework.RawConfigInstance
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.createDirectories

class DefaultFileSynchronizer @Inject internal constructor(
    @Named("config") private val configDir: Path,
    private val fileSynchronizerFactory: FileConfigInstanceSynchronizer.Factory
) : ConfigSynchronizer<Any?> {

    override fun apply(instance: RawConfigInstance<Any?>) {
        val type = instance.type

        val schemaDir = configDir.resolve("schemas")
        schemaDir.createDirectories()
        val fileName = "${type.namespace}_${type.name}"
        val savePath = configDir.resolve("$fileName.json")
        val schemaPath = schemaDir.resolve("$fileName.schema.json")

        fileSynchronizerFactory.newFileSynchronizer(instance, savePath, schemaPath) {}
    }
}

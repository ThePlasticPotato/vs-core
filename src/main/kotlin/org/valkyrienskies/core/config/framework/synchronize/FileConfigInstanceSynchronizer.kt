package org.valkyrienskies.core.config.framework.synchronize

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.valkyrienskies.core.config.framework.ConfigInstance
import org.valkyrienskies.core.config.framework.ScopedConfig
import org.valkyrienskies.core.config.framework.exceptions.ConfigException
import org.valkyrienskies.core.util.files.FileWatcher
import org.valkyrienskies.core.util.logger
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.outputStream
import kotlin.io.path.relativeTo

internal class FileConfigInstanceSynchronizer<X>(
    private val instance: ConfigInstance<*, X>,
    savePath: Path,
    schemaPath: Path,
    private val contextProvider: ScopeContextProvider<X>,
    private val fileWatcher: FileWatcher,
    private val mapper: ObjectMapper,
) : ConfigInstanceSynchronizer<Nothing, X> {

    class Factory @Inject constructor(
        @Named("config") private val mapper: ObjectMapper,
        private val fileWatcher: FileWatcher,
    ) {
        fun <C : ScopedConfig<X>, X> newFileSynchronizer(
            instance: ConfigInstance<C, X>,
            savePath: Path,
            schemaPath: Path,
            contextProvider: ScopeContextProvider<X>
        ) = FileConfigInstanceSynchronizer(instance, savePath, schemaPath, contextProvider, fileWatcher, mapper)
    }

    fun interface ScopeContextProvider<out X> {

        fun getContext(path: Path): X
    }

    companion object {
        private val logger by logger()
    }

    private val savePath = savePath.toAbsolutePath()
    private val schemaPath = schemaPath.toAbsolutePath()

    init {
        if (savePath.notExists()) {
            writeFiles()
        } else {
            updateFromFile()
        }
        watchSave()
    }

    private fun watchSave() {
        fileWatcher.startWatchingFile(savePath) {
            logger.info("Config file changed, attempting update: $savePath")
            updateFromFile()
        }
    }

    private fun updateFromFile() {
        if (savePath.isDirectory()) {
            throw ConfigException("Save path is directory: $savePath")
        }

        val json = mapper.readTree(savePath.inputStream()) as ObjectNode
        json.remove("\$schema")

        val context = contextProvider.getContext(savePath)

        instance.attemptUpdate(json, context)
    }

    private fun writeFiles() {
        if (schemaPath.notExists()) {
            // write schema
            mapper.writeValue(schemaPath.outputStream(), instance.type.schemaJson)
        }

        val schemaRelativePath = schemaPath.relativeTo(savePath.parent).toString()
        val json = instance.serialize()
        json.put("\$schema", schemaRelativePath)
        mapper.writeValue(savePath.outputStream(), json)

        return
    }

    override fun afterUpdate(config: ConfigInstance<Nothing, X>, ctx: X) {
        logger.info("VS core updated config file, attempting to write to disk: $savePath")
        writeFiles()
    }
}

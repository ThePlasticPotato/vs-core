package org.valkyrienskies.core.util.files

import java.nio.file.Path
import java.util.function.Consumer

interface FileWatcher : AutoCloseable {

    fun startWatchingFile(path: Path, onUpdate: Consumer<Path>)
}

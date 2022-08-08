package org.valkyrienskies.core.util.files

import dagger.Reusable
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.inject.Inject
import kotlin.concurrent.thread

// modified from https://stackoverflow.com/a/27737069 and https://stackoverflow.com/a/25221600
@Reusable
class FileWatcherImpl @Inject constructor() : FileWatcher {

    init {
        thread(start = true, isDaemon = true) { run() }
    }

    @Volatile
    private var stop: Boolean = false

    private val watched = HashMap<Path, Consumer<Path>>()
    private val toWatch = ConcurrentLinkedQueue<Pair<Path, Consumer<Path>>>()

    override fun startWatchingFile(path: Path, onUpdate: Consumer<Path>) {
        toWatch.add(Pair(path.toAbsolutePath(), onUpdate))
    }

    override fun close() {
        stop = true
    }

    private fun run() {
        try {
            FileSystems.getDefault().newWatchService().use { watcher ->
                while (!stop) {
                    toWatch.forEach { (path, consumer) ->
                        path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
                        watched[path] = consumer
                    }

                    val key: WatchKey = try {
                        watcher.poll(100, TimeUnit.MILLISECONDS)
                    } catch (e: InterruptedException) {
                        return
                    } ?: continue

                    // Prevent receiving two separate ENTRY_MODIFY events: file modified
                    // and timestamp updated. Instead, receive one ENTRY_MODIFY event
                    // with two counts.
                    Thread.sleep(50)

                    for (event in key.pollEvents()) {
                        val kind: WatchEvent.Kind<*> = event.kind()

                        if (kind === StandardWatchEventKinds.OVERFLOW) {
                            continue
                        } else if (kind === StandardWatchEventKinds.ENTRY_MODIFY) {
                            val modified = (event.context() as Path).toAbsolutePath()
                            watched[modified]?.accept(modified)
                        }

                        val valid: Boolean = key.reset()
                        if (!valid) {
                            break
                        }
                    }
                    Thread.yield()
                }
            }
        } catch (e: Throwable) {
            // Log or rethrow the error
        }
    }

    protected fun finalize() {
        close()
    }
}

package org.valkyrienskies.core.util

import dagger.Binds
import dagger.Module
import org.valkyrienskies.core.util.files.FileWatcher
import org.valkyrienskies.core.util.files.FileWatcherImpl
import org.valkyrienskies.core.util.serialization.VSJacksonModule

@Module(includes = [VSJacksonModule::class])
abstract class VSCoreUtilModule {
    @Binds
    abstract fun fileWatcher(impl: FileWatcherImpl): FileWatcher
}

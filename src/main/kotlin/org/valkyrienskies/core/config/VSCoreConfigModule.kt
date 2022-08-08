package org.valkyrienskies.core.config

import dagger.Module
import dagger.Provides
import org.valkyrienskies.core.config.VSCoreConfig.Server
import org.valkyrienskies.core.config.framework.scopes.single.SingleConfig
import org.valkyrienskies.core.config.framework.scopes.single.SingleConfigRegistry
import org.valkyrienskies.core.config.framework.scopes.single.SingleConfigRegistryImpl
import org.valkyrienskies.core.config.framework.synchronize.DefaultFileSynchronizer
import org.valkyrienskies.core.hooks.AbstractCoreHooks
import java.nio.file.Path
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class VSCoreConfigModule {
    @Provides
    @Named("config")
    fun configDir(hooks: AbstractCoreHooks): Path = hooks.configDir

    @Provides
    @Singleton
    @ServerConfig
    fun serverRegistry(
        factory: SingleConfigRegistryImpl.Factory,
        fileSynchronizer: DefaultFileSynchronizer
    ): SingleConfigRegistry = factory.newSingleConfigRegistry(listOf(fileSynchronizer))

    @Provides
    @Singleton
    fun server(@ServerConfig configRegistry: SingleConfigRegistry): SingleConfig<Server> =
        configRegistry.registerAndGet("vs_core:server", Server::class.java)

    @Provides
    fun serverInstance(serverConfig: SingleConfig<Server>) = serverConfig.get()

    @Qualifier
    annotation class ApplicationConfig

    @Qualifier
    annotation class ServerConfig
}

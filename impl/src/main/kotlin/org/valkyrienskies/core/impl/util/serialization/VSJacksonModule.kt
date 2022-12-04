package org.valkyrienskies.core.impl.util.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class VSJacksonModule {

    @Provides
    fun defaultMapper(): ObjectMapper = VSJacksonUtil.defaultMapper

    @Provides
    @Named("dto")
    fun dtoMapper(): ObjectMapper = VSJacksonUtil.dtoMapper

    @Provides
    @Named("config")
    fun configMapper(): ObjectMapper = VSJacksonUtil.configMapper

    @Provides
    @Named("delta")
    fun deltaMapper(): ObjectMapper = VSJacksonUtil.deltaMapper

    @Provides
    @Named("packet")
    fun packetMapper(): ObjectMapper = VSJacksonUtil.packetMapper
}

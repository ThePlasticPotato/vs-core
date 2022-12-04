package org.valkyrienskies.core.impl.util.serialization

import com.fasterxml.jackson.databind.module.SimpleModule
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSet

class FastUtilModule : SimpleModule() {

    init {
        addAbstractTypeMapping<LongSet, LongOpenHashSet>()
        addMapping<LongOpenHashSet, LongArray>({ it.toLongArray() }, { LongOpenHashSet(it) })
    }
}

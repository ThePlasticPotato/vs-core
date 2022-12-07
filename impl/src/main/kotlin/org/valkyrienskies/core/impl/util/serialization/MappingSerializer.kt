package org.valkyrienskies.core.impl.util.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class MappingSerializer<A, B>(
    private val mapper: (A) -> B,
    type: Class<A>
) : StdSerializer<A>(type) {
    override fun serialize(value: A, gen: JsonGenerator, provider: SerializerProvider) {
        provider.defaultSerializeValue(mapper(value), gen)
    }
}

class MappingDeserializer<A, B>(
    private val mapper: (B) -> A,
    typeA: Class<A>,
    private val typeB: Class<B>
) : StdDeserializer<A>(typeA) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): A {
        return mapper(p.codec.readValue(p, typeB))
    }
}

inline fun <reified A, reified B> SimpleModule.addMapping(
    noinline preSerialize: (A) -> B,
    noinline postDeserialize: (B) -> A
) {
    addSerializer(A::class.java, MappingSerializer(preSerialize, A::class.java))
    addDeserializer(A::class.java, MappingDeserializer(postDeserialize, A::class.java, B::class.java))
}

package org.valkyrienskies.core.util.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken.END_OBJECT
import com.fasterxml.jackson.core.JsonToken.FIELD_NAME
import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.google.common.collect.ClassToInstanceMap
import com.google.common.collect.MutableClassToInstanceMap
import java.text.ParseException

class GuaveSerializationModule : SimpleModule() {

    init {
        addSerializer(MutableClassToInstanceMap::class.java, ClassToInstanceMapSerializer)
        addDeserializer(MutableClassToInstanceMap::class.java, ClassToInstanceMapDeserializer)

        addAbstractTypeMapping<ClassToInstanceMap<*>, MutableClassToInstanceMap<*>>()
    }

    private inline fun <reified A, reified B : A> addAbstractTypeMapping() {
        super.addAbstractTypeMapping(A::class.java, B::class.java)
    }

    private object ClassToInstanceMapSerializer :
        StdSerializer<MutableClassToInstanceMap<*>>(MutableClassToInstanceMap::class.java) {

        override fun serialize(
            value: MutableClassToInstanceMap<*>, gen: JsonGenerator, provider: SerializerProvider
        ) {
            gen.writeStartObject()
            for (entry in value.entries) {
                gen.writeFieldName(entry.key.name)
                provider.defaultSerializeValue(entry.value, gen)
            }
            gen.writeEndObject()
        }
    }

    private object ClassToInstanceMapDeserializer :
        StdDeserializer<MutableClassToInstanceMap<*>>(MutableClassToInstanceMap::class.java) {

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MutableClassToInstanceMap<*> {
            if (p.currentToken != START_OBJECT) throw ParseException(
                "Expected start of object", p.currentLocation.lineNr
            )

            val map = MutableClassToInstanceMap.create<Any>()
            var token = p.nextToken()
            while (token != END_OBJECT) {
                if (token != FIELD_NAME) throw ParseException(
                    "Expected field name", p.currentLocation.lineNr
                )

                val clazz = Class.forName(p.currentName)
                token = p.nextToken()
                val value = p.codec.readValue(p, clazz)

                map[clazz] = value
                token = p.nextToken()
            }

            return map
        }
    }
}

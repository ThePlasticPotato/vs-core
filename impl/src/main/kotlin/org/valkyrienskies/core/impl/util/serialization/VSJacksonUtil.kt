package org.valkyrienskies.core.impl.util.serialization

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.AnnotationIntrospector
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import org.valkyrienskies.core.impl.game.ships.ShipData
import org.valkyrienskies.core.impl.game.ships.ShipDataCommon
import java.io.InputStream

object VSJacksonUtil {
    /**
     * the default mapper for the standard Valkyrien Skies configuration for serializing
     * things, particularly [org.valkyrienskies.core.impl.game.ShipData]
     */
    val defaultMapper = CBORMapper()

    /**
     * the mapper for Valkyrien Skies network transmissions (e.g., it ignores
     * [org.valkyrienskies.core.impl.util.serialization.PacketIgnore] annotated fields
     */
    val packetMapper = CBORMapper()

    /**
     * the mapper for serializing delta updates to ShipData
     * It ignores [org.valkyrienskies.core.impl.util.serialization.DeltaIgnore]
     */
    val deltaMapper = CBORMapper()

    /**
     * the mapper for configuration data
     */
    val configMapper = ObjectMapper()

    /**
     * The mapper to use for DTOs. Uses strict settings - only uses public getters/setters,
     * fails on unknown properties, etc.
     */
    val dtoMapper = ObjectMapper()

    init {
        // Configure the mappers
        configureMapper(defaultMapper)
        configurePacketMapper(packetMapper)
        configureDeltaMapper(deltaMapper)
        configureConfigMapper(configMapper)
        configureDtoMapper(dtoMapper)
    }

    fun configureAll(configure: (ObjectMapper) -> Unit) {
        configure(defaultMapper)
        configure(packetMapper)
        configure(deltaMapper)
        configure(configMapper)
    }

    @JsonSerialize(`as` = ShipDataCommon::class)
    private object ShipDataServerMixin

    private fun configureConfigMapper(mapper: ObjectMapper) {
        configureMapper(mapper)
        mapper.enable(INDENT_OUTPUT)
    }

    private fun configurePacketMapper(mapper: ObjectMapper) {
        configureMapper(mapper)
        mapper.insertAnnotationIntrospector(IgnoringAnnotationIntrospector(PacketIgnore::class.java))
        mapper.addMixIn(ShipData::class.java, ShipDataServerMixin::class.java)
    }

    private fun configureDeltaMapper(mapper: ObjectMapper) {
        configureMapper(mapper)
        mapper.insertAnnotationIntrospector(
            IgnoringAnnotationIntrospector(PacketIgnore::class.java, DeltaIgnore::class.java)
        )
        mapper.addMixIn(ShipData::class.java, ShipDataServerMixin::class.java)
    }

    private fun configureDtoMapper(mapper: ObjectMapper) {
        registerStandardModules(mapper)
        mapper.setVisibility(
            mapper.visibilityChecker
                .withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
        )
    }

    /**
     * Configures the selected object mapper to use the standard Valkyrien Skies configuration for
     * serializing things
     *
     * @param mapper The ObjectMapper to configure
     */
    private fun configureMapper(mapper: ObjectMapper) {
        registerStandardModules(mapper)

        mapper.setVisibility(
            mapper.visibilityChecker
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        )

        mapper.registerModule(FastUtilModule())

        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    /**
     * Registers standard VS serialization modules
     */
    private fun registerStandardModules(mapper: ObjectMapper) {
        mapper
            .registerModule(JOMLSerializationModule())
            .registerModule(VSSerializationModule())
            .registerModule(GuaveSerializationModule())
            .registerKotlinModule()
    }
}

inline fun <reified T> ObjectMapper.readValue(buf: ByteBuf): T {
    return readValue(ByteBufInputStream(buf) as InputStream)
}

fun <T> ObjectMapper.readValue(buf: ByteBuf, clazz: Class<T>): T {
    return readValue(ByteBufInputStream(buf) as InputStream, clazz)
}

fun ObjectNode.shallowCopy(): ObjectNode {
    val ret = objectNode()
    for ((key, value) in fields())
        ret.replace(key, value)
    return ret
}

fun ObjectNode.shallowCopyWith(key: String, value: JsonNode) =
    shallowCopy().also { it.replace(key, value) }

inline fun <reified A, reified B : A> SimpleModule.addAbstractTypeMapping(): SimpleModule =
    addAbstractTypeMapping(A::class.java, B::class.java)

inline fun <reified A, reified B> SimpleModule.setMixInAnnotation(): SimpleModule =
    setMixInAnnotation(A::class.java, B::class.java)

fun ObjectMapper.insertAnnotationIntrospector(ai: AnnotationIntrospector) {
    this.registerModule(object : SimpleModule() {
        override fun setupModule(context: SetupContext) {
            context.insertAnnotationIntrospector(ai)
            super.setupModule(context)
        }
    })
}

/**
 * The source overrides the target
 */
fun merge(target: ObjectNode, source: ObjectNode): ObjectNode {
    val new = target.shallowCopy()
    for ((key, value) in source.fields()) {
        if (value.isObject && new.has(key) && new[key].isObject) {
            new.replace(key, merge(new[key] as ObjectNode, value as ObjectNode))
        } else {
            new.replace(key, value.deepCopy())
        }
    }
    return new
}

fun ObjectMapper.appendAnnotationIntrospector(ai: AnnotationIntrospector) {
    this.registerModule(object : SimpleModule() {
        override fun setupModule(context: SetupContext) {
            context.appendAnnotationIntrospector(ai)
            super.setupModule(context)
        }
    })
}

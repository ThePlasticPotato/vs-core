package org.valkyrienskies.core.config.framework

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.imifou.jsonschema.module.addon.AddonModule
import com.github.victools.jsonschema.generator.Option.ADDITIONAL_FIXED_TYPES
import com.github.victools.jsonschema.generator.Option.ALLOF_CLEANUP_AT_THE_END
import com.github.victools.jsonschema.generator.Option.EXTRA_OPEN_API_FORMAT_VALUES
import com.github.victools.jsonschema.generator.Option.FLATTENED_ENUMS
import com.github.victools.jsonschema.generator.Option.FLATTENED_OPTIONALS
import com.github.victools.jsonschema.generator.Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT
import com.github.victools.jsonschema.generator.Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES
import com.github.victools.jsonschema.generator.Option.NONPUBLIC_NONSTATIC_FIELDS_WITH_GETTERS
import com.github.victools.jsonschema.generator.Option.PUBLIC_NONSTATIC_FIELDS
import com.github.victools.jsonschema.generator.Option.SCHEMA_VERSION_INDICATOR
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import com.github.victools.jsonschema.module.jackson.JacksonModule
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import org.checkerframework.checker.units.qual.C
import javax.inject.Inject
import javax.inject.Named

class ConfigType<out C : ScopedConfig<X>, in X>(
    val namespace: String,
    val name: String,
    val clazz: Class<out C>,
    val schemaJson: ObjectNode,
    val schemaValidator: JsonSchema,
    val defaultInstance: C,
    val mapper: ObjectMapper
) {
    class Factory @Inject constructor(
        @Named("config") private val mapper: ObjectMapper
    ) {
        companion object {
            private val JSON_SCHEMA_GENERATOR_VERSION = SchemaVersion.DRAFT_2019_09
            private val JSON_SCHEMA_VALIDATOR_VERSION = SpecVersion.VersionFlag.V201909
        }

        private val schemaGenerator = SchemaGenerator(
            SchemaGeneratorConfigBuilder(
                mapper,
                JSON_SCHEMA_GENERATOR_VERSION,
                OptionPreset(
                    SCHEMA_VERSION_INDICATOR,
                    ADDITIONAL_FIXED_TYPES,
                    EXTRA_OPEN_API_FORMAT_VALUES,
                    FLATTENED_ENUMS,
                    FLATTENED_OPTIONALS,
                    PUBLIC_NONSTATIC_FIELDS,
                    NONPUBLIC_NONSTATIC_FIELDS_WITH_GETTERS,
                    MAP_VALUES_AS_ADDITIONAL_PROPERTIES,
                    FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT,
                    ALLOF_CLEANUP_AT_THE_END
                )
            ).with(AddonModule()).with(JacksonModule()).build()
        )

        private fun <C : Any> createDefaultInstance(clazz: Class<C>): C {
            return mapper.readValue("{}", clazz)
        }

        fun <C : ScopedConfig<X>, X> create(
            namespace: String,
            name: String,
            clazz: Class<C>,
            defaultInstance: C? = null
        ): ConfigType<C, X> {
            val schemaJson = schemaGenerator.generateSchema(clazz)
            (schemaJson.get("properties") as? ObjectNode)?.putObject("\$schema")?.put("type", "string")
            val schemaValidator = JsonSchemaFactory.getInstance(JSON_SCHEMA_VALIDATOR_VERSION).getSchema(schemaJson)
            return ConfigType(
                namespace, name, clazz, schemaJson, schemaValidator,
                defaultInstance ?: createDefaultInstance(clazz), mapper
            )
        }
    }
}

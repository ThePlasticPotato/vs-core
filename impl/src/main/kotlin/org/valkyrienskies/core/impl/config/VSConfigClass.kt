package org.valkyrienskies.core.impl.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.imifou.jsonschema.module.addon.AddonModule
import com.github.victools.jsonschema.generator.*
import com.github.victools.jsonschema.module.jackson.JacksonModule
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import com.rubydesic.jacksonktdsl.obj
import org.valkyrienskies.core.apigame.hooks.PlayState
import org.valkyrienskies.core.apigame.world.IPlayer
import org.valkyrienskies.core.impl.config.VSConfigClass.VSConfigClassSide.*
import org.valkyrienskies.core.impl.hooks.CoreHooks
import org.valkyrienskies.core.impl.networking.impl.PacketCommonConfigUpdate
import org.valkyrienskies.core.impl.networking.impl.PacketServerConfigUpdate
import org.valkyrienskies.core.impl.networking.simple.*
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil
import org.valkyrienskies.core.impl.util.serialization.merge
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

data class VSConfigClass(
    val clazz: Class<*>,
    val name: String,
    val client: SidedVSConfigClass?,
    val common: SidedVSConfigClass?,
    val server: SidedVSConfigClass?
) {

    val sides = listOfNotNull(client, common, server)

    private fun makeServerConfigUpdatePacket(): PacketServerConfigUpdate? {
        if (server == null) return null
        return PacketServerConfigUpdate(clazz, mapper.valueToTree(server.inst))
    }

    private fun makeCommonConfigUpdatePacket(): PacketCommonConfigUpdate? {
        if (common == null) return null
        return PacketCommonConfigUpdate(clazz, mapper.valueToTree(common.inst))
    }

    fun readOrCreateConfig() {
        sides.forEach { it.readOrCreateConfig(CoreHooks.configDir) }
    }

    fun writeToDisk() {
        if (CoreHooks.isPhysicalClient) {
            client?.saveConfig()
        }

        if (CoreHooks.playState != PlayState.CLIENT_MULTIPLAYER) {
            server?.saveConfig()
            client?.saveConfig()
        }
    }

    fun syncToServer() {
        if (CoreHooks.playState == PlayState.CLIENT_MULTIPLAYER) {
            makeServerConfigUpdatePacket()?.sendToServer()
            makeCommonConfigUpdatePacket()?.sendToServer()
        }
    }

    companion object {
        val mapper = VSJacksonUtil.configMapper

        private val registeredConfigMap = HashMap<Class<*>, VSConfigClass>()

        private val JSON_SCHEMA_GENERATOR_VERSION = SchemaVersion.DRAFT_2019_09
        private val JSON_SCHEMA_VALIDATOR_VERSION = SpecVersion.VersionFlag.V201909

        private val schemaGeneratorConfig = SchemaGeneratorConfigBuilder(
            mapper,
            JSON_SCHEMA_GENERATOR_VERSION,
            OptionPreset(
                Option.SCHEMA_VERSION_INDICATOR,
                Option.ADDITIONAL_FIXED_TYPES,
                Option.EXTRA_OPEN_API_FORMAT_VALUES,
                Option.FLATTENED_ENUMS,
                Option.FLATTENED_OPTIONALS,
                Option.PUBLIC_NONSTATIC_FIELDS,
                Option.NONPUBLIC_NONSTATIC_FIELDS_WITH_GETTERS,
                Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES,
                Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT,
                Option.ALLOF_CLEANUP_AT_THE_END
            )
        ).with(AddonModule()).with(JacksonModule()).build()

        fun afterDisconnect() {
            // Update all config values to singleplayer values when the player disconnects
            registeredConfigMap.values.forEach { config ->
                config.readOrCreateConfig()
            }
        }

        fun afterClientJoinServer(player: IPlayer) {
            if (CoreHooks.isPhysicalClient) return

            registeredConfigMap.values.forEach { config ->
                config.makeCommonConfigUpdatePacket()?.sendToClient(player)

                if (player.canModifyServerConfig) {
                    config.makeServerConfigUpdatePacket()?.sendToClient(player)
                }
            }
        }

        fun UpdatableConfig.writeToDisk() = getMainConfig().writeToDisk()

        private fun UpdatableConfig.getMainConfig(): VSConfigClass =
            getRegisteredConfig(if (getSide() == null) this::class.java else this::class.java.enclosingClass)

        private fun UpdatableConfig.getSide(): VSConfigClassSide? =
            VSConfigClassSide.VALUES.find { it.subclassName == this::class.java.name }

        fun UpdatableConfig.syncConfigToClient() {
            when (getSide()) {
                null -> {
                    val registeredConfig = getRegisteredConfig(this::class.java)

                    registeredConfig.makeCommonConfigUpdatePacket()?.sendToAllClients()
                }

                CLIENT -> {
                    throw IllegalArgumentException("Cannot sync client config to client")
                }

                SERVER -> {
                    throw IllegalArgumentException("Currently syncing server config to client is unsupported...")
                }

                COMMON -> {
                    val registeredConfig = getRegisteredConfig(this::class.java.enclosingClass)
                    registeredConfig.makeCommonConfigUpdatePacket()!!.sendToAllClients()
                }
            }
        }

        fun UpdatableConfig.syncConfigToServer() {
            when (getSide()) {
                null -> {
                    getRegisteredConfig(this::class.java).syncToServer()
                }

                CLIENT -> {
                    throw IllegalArgumentException("Cannot sync client config to server")
                }

                SERVER -> {
                    val registeredConfig = getRegisteredConfig(this::class.java.enclosingClass)
                    registeredConfig.makeServerConfigUpdatePacket()!!.sendToServer()
                }

                COMMON -> {
                    val registeredConfig = getRegisteredConfig(this::class.java.enclosingClass)
                    registeredConfig.makeCommonConfigUpdatePacket()!!.sendToServer()
                }
            }
        }

        fun getRegisteredConfig(mainClass: Class<*>): VSConfigClass {
            return requireNotNull(registeredConfigMap[mainClass]) {
                "This UpdatableConfig ($mainClass) is not registered as a main config"
            }
        }

        private fun attemptUpdate(
            mainClass: Class<*>, newConfig: JsonNode, side: (VSConfigClass) -> SidedVSConfigClass?
        ) {
            try {
                val config = side(getRegisteredConfig(mainClass)) ?: return
                val errors = config.attemptUpdate(newConfig)
                if (errors.isNotEmpty()) {
                    println("Attempted to update config with invalid schema:\n$errors")
                    return
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        fun registerNetworkHandlers() {
            println("registering network handlers")
            PacketServerConfigUpdate::class.registerServerHandler { (mainClass, newConfig), player ->
                println("attempt server update with $mainClass and $newConfig")
                if (player.canModifyServerConfig) {
                    attemptUpdate(mainClass, newConfig) { it.server }
                    getRegisteredConfig(mainClass).server?.saveConfig()
                }
            }
            PacketServerConfigUpdate::class.registerClientHandler { (mainClass, newConfig) ->
                attemptUpdate(mainClass, newConfig) { it.server }
            }
            PacketCommonConfigUpdate::class.registerServerHandler { (mainClass, newConfig), player ->
                if (player.canModifyServerConfig) {
                    attemptUpdate(mainClass, newConfig) { it.common }
                    getRegisteredConfig(mainClass).common?.saveConfig()
                }
            }
            PacketCommonConfigUpdate::class.registerClientHandler { (mainClass, newConfig) ->
                attemptUpdate(mainClass, newConfig) { it.common }
            }
        }

        private fun getSidedConfigClassAndInstance(side: VSConfigClassSide, mainClass: Class<*>): Pair<Class<*>, Any>? {
            val sidedConfigField =
                mainClass.fields.find { Modifier.isStatic(it.modifiers) && it.name == side.fieldName } ?: return null

            val instance = sidedConfigField.get(null) ?: throw IllegalArgumentException(
                "Type B (Java Class) main config class ($mainClass) property $sidedConfigField is null!"
            )

            return Pair(sidedConfigField.type, instance)
        }

        private fun createSidedVSConfigClass(
            parentName: String, side: VSConfigClassSide, mainClass: Class<*>
        ): SidedVSConfigClass? {
            val (clazz, inst) = getSidedConfigClassAndInstance(side, mainClass) ?: return null

            val generator = SchemaGenerator(schemaGeneratorConfig)

            val schemaJson = generator.generateSchema(clazz)
            (schemaJson.get("properties") as? ObjectNode)?.putObject("\$schema")?.put("type", "string")

            val schema = JsonSchemaFactory.getInstance(JSON_SCHEMA_VALIDATOR_VERSION).getSchema(schemaJson)

            val onUpdate = if (inst is UpdatableConfig) inst::onUpdate else ({})

            return SidedVSConfigClass(
                clazz, inst, side.subclassName, parentName, schemaJson, schema, onUpdate
            )
        }

        fun registerConfig(name: String, clazz: Class<*>): VSConfigClass {
            val registered = registeredConfigMap[clazz]
            if (registered != null) throw IllegalArgumentException("Already registered")

            val client = createSidedVSConfigClass(name, CLIENT, clazz)
            val common = createSidedVSConfigClass(name, COMMON, clazz)
            val server = createSidedVSConfigClass(name, SERVER, clazz)

            val configClass = VSConfigClass(clazz, name, client, common, server)
            configClass.readOrCreateConfig()

            registeredConfigMap[clazz] = configClass

            return configClass
        }
    }

    private enum class VSConfigClassSide(
        val subclassName: String, val fieldName: String = subclassName.uppercase()
    ) {
        CLIENT("Client"), COMMON("Common"), SERVER("Server");

        companion object {
            val VALUES = values().toList()
        }
    }
}

class SidedVSConfigClass(
    val clazz: Class<*>, val inst: Any, val sideName: String, val parentName: String, val schemaJson: ObjectNode,
    val schema: JsonSchema, val onUpdate: () -> Unit
) {

    fun generateInstJson(): ObjectNode = VSConfigClass.mapper.valueToTree(inst)
    fun generateInstJsonAndMergeWith(key: String, value: JsonNode) = merge(generateInstJson(), obj { key to value })

    fun attemptUpdate(newConfig: JsonNode): Set<ValidationMessage> {
        val errors = schema.validate(newConfig as ObjectNode)
        if (errors.isNotEmpty()) {
            return errors
        }
        VSConfigClass.mapper.readerForUpdating(inst).withoutAttribute("\$schema").readValue(newConfig, clazz)
        onUpdate()

        return emptySet()
    }

    fun saveConfig(configDir: Path = CoreHooks.configDir) {
        val mapper = VSConfigClass.mapper

        val name = "${parentName}_$sideName".lowercase()
        val schemaRelativePath = "schemas/$name.schema.json"
        val configPath = configDir.resolve("$name.json")
        val json = mapper.valueToTree<ObjectNode>(inst)

        json.put("\$schema", schemaRelativePath)
        mapper.writeValue(Files.newBufferedWriter(configPath), json)
    }

    /**
     * @return an error message if the schema validation failed
     */
    fun readOrCreateConfig(configDir: Path): String? {
        val mapper = VSConfigClass.mapper

        val name = "${parentName}_$sideName".lowercase()
        val schemaRelativePath = "schemas/$name.schema.json"
        val schemaPath = configDir.resolve(schemaRelativePath)
        val configPath = configDir.resolve("$name.json")

        Files.createDirectories(configDir.resolve("schemas"))
        mapper.writeValue(Files.newBufferedWriter(schemaPath), schemaJson)

        if (Files.exists(configPath)) {
            try {
                val json = mapper.readTree(Files.newBufferedReader(configPath)) as ObjectNode
                json.remove("\$schema")
                val errors = attemptUpdate(json)

                if (errors.isNotEmpty()) {
                    return errors.joinToString(separator = "\n")
                }

                return null
            } catch (ex: JsonProcessingException) {
                return ex.message
            }
        }

        val json = mapper.valueToTree<ObjectNode>(inst)
        json.put("\$schema", schemaRelativePath)
        mapper.writeValue(Files.newBufferedWriter(configPath, StandardOpenOption.CREATE_NEW), json)

        return null
    }
}

package org.valkyrienskies.core.config.framework

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.valkyrienskies.core.config.framework.exceptions.ConfigException
import org.valkyrienskies.core.config.framework.exceptions.ConfigValidationException
import org.valkyrienskies.core.config.framework.scopes.single.SingleScopedConfig
import org.valkyrienskies.core.config.framework.synchronize.ConfigInstanceSynchronizer
import org.valkyrienskies.core.networking.RegisteredHandler
import org.valkyrienskies.core.util.serialization.shallowCopyWith
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

typealias SingletonConfigInstance = ConfigInstance<SingleScopedConfig, Unit>
typealias ConfigInstanceId = String

class ConfigInstanceImpl<C : ScopedConfig<X>, X> internal constructor(
    override val type: ConfigType<C, X>,
    private val mapper: ObjectMapper,
    override val id: ConfigInstanceId
) : ConfigInstance<C, X> {
    @Volatile
    private var inst: C = type.defaultInstance
    private val synchronizers = ConcurrentHashMap.newKeySet<ConfigInstanceSynchronizer<X>>()

    private val updateLock = ReentrantLock()

    override val value get() = inst

    override fun serialize(): ObjectNode = mapper.valueToTree(inst)

    override fun serializeWith(key: String, newValue: JsonNode) = serialize().shallowCopyWith(key, newValue)

    override fun registerSynchronizer(synchronizer: ConfigInstanceSynchronizer<X>): RegisteredHandler {
        synchronizers.add(synchronizer)
        return RegisteredHandler { synchronizers.remove(synchronizer) }
    }

    /**
     * Attempt to update this [ConfigInstance] to the value in [newValue].
     *
     * @throws ConfigValidationException If config validation fails OR the onUpdate method throws but is successfully reverted
     * (the original onUpdate is called and doesn't throw)
     *
     * @throws ConfigException If config validation fails and the original onUpdate throws.
     */
    override fun attemptUpdate(newValue: ObjectNode, ctx: X) = updateLock.withLock {
        val schemaValidationErrors = type.schemaValidator.validate(newValue)

        if (schemaValidationErrors.isNotEmpty()) {
            throw ConfigValidationException(schemaValidationErrors.joinToString())
        }

        val newInstance = mapper.treeToValue(newValue, type.clazz)
        val validationError = newInstance.validate(ctx)

        if (validationError != null) {
            throw ConfigValidationException(validationError)
        }

        try {
            newInstance.onUpdate(ctx)
        } catch (ex1: Exception) {
            try {
                inst.onUpdate(ctx)
                throw ConfigValidationException("Config passed validation but onUpdate threw", ex1)
            } catch (ex2: Exception) {
                throw ConfigException("Failed to revert config after onUpdate threw (it threw again)", ex2)
                    .apply { addSuppressed(ex1) }
            }
        }

        inst = newInstance
        synchronizers.forEach { it.afterUpdate(this, ctx) }
    }

    /**
     * Serializes the provided value and then passes it to [attemptUpdate]
     */
    override fun attemptUpdate(newValue: Any, ctx: X) =
        attemptUpdate(mapper.valueToTree(newValue), ctx)
}

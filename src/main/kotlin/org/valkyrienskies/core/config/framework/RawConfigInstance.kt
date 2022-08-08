package org.valkyrienskies.core.config.framework

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.valkyrienskies.core.config.framework.synchronize.ConfigInstanceSynchronizer
import org.valkyrienskies.core.networking.RegisteredHandler

interface RawConfigInstance<in X> {
    val value: ScopedConfig<*>
    val id: ConfigInstanceId
    fun serialize(): ObjectNode
    fun serializeWith(key: String, newValue: JsonNode): ObjectNode
    fun registerSynchronizer(synchronizer: ConfigInstanceSynchronizer<@UnsafeVariance X>): RegisteredHandler

    /**
     * Attempt to update this [ConfigInstance] to the value in [newValue].
     *
     * @throws ConfigValidationException If config validation fails OR the onUpdate method throws but is successfully reverted
     * (the original onUpdate is called and doesn't throw)
     *
     * @throws ConfigException If config validation fails and the original onUpdate throws.
     */
    fun attemptUpdate(newValue: ObjectNode, ctx: X)

    /**
     * Serializes the provided value and then passes it to [attemptUpdate]
     */
    fun attemptUpdate(newValue: Any, ctx: X)
}

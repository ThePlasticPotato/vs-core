package org.valkyrienskies.core.config.framework

/**
 * Implemented by config classes in the VS config system.
 *
 * Notes:
 * - **Config classes should be immutable.**
 *
 * @param Context Additional context provided by the scope. [Unit] if no context is provided
 */
interface ScopedConfig<in Context : ConfigContext> {

    /**
     * Called when the config is updated. This should NOT change any config values.
     * (in fact, it shouldn't be possible because config classes should be immutable...)
     *
     * Notes:
     * - **There are no guarantees as to which thread this is called on**
     *
     * @throws Exception If this method throws, the current config will be discarded and the config will be updated
     * to its old state. If it throws again, the exception will be propagated.
     */
    fun onUpdate(ctx: Context) {}

    /**
     * Checks the config values and returns a [String] containing an error message if they are incorrect.
     *
     *
     * Notes:
     * - **This method must be idempotent**
     * - **There are no guarantees as to which thread this is called on**
     * - It is recommended to use [JsonSchema] validation instead of this whenever possible, as
     * that will have better UX for the user.
     *
     * @throws Exception If this method throws, the exception will be caught and used as an error message.
     */
    fun validate(ctx: Context): String? = null
}

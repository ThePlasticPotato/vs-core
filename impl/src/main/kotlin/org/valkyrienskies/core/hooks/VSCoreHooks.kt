package org.valkyrienskies.core.hooks

import org.valkyrienskies.core.api.hooks.CoreHooksIn
import org.valkyrienskies.core.api.hooks.CoreHooksOut
import org.valkyrienskies.core.api.world.IPlayer
import org.valkyrienskies.core.config.VSConfigClass
import javax.inject.Inject

class VSCoreHooks @Inject constructor(
    hooksOut: CoreHooksOut
) : CoreHooksOut by hooksOut, CoreHooksIn {

    /**
     * Called when client disconnects from a world
     */
    override fun afterDisconnect() {
        VSConfigClass.afterDisconnect()
    }

    /**
     * Called when a client joins a server
     */
    override fun afterClientJoinServer(player: IPlayer) {
        VSConfigClass.afterClientJoinServer(player)
    }
}

lateinit var CoreHooks: VSCoreHooks

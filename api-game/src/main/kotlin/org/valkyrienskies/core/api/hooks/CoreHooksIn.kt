package org.valkyrienskies.core.api.hooks

import org.valkyrienskies.core.api.world.IPlayer

interface CoreHooksIn {

    fun afterDisconnect()

    fun afterClientJoinServer(player: IPlayer)
}
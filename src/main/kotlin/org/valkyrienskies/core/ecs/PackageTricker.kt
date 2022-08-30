package dev.dominion.ecs.engine

import dev.dominion.ecs.api.Entity

fun Entity.delete() = (this as IntEntity).delete()

package org.valkyrienskies.test_utils

import io.kotest.core.config.AbstractProjectConfig

object KotestConfig : AbstractProjectConfig() {

    override val parallelism: Int = Runtime.getRuntime().availableProcessors()

}
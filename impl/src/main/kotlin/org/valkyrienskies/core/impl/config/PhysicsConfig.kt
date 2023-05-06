package org.valkyrienskies.core.impl.config

import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema
import org.valkyrienskies.physics_api_krunch.SolverType

class PhysicsConfig {

    @JsonSchema(
        description = "The detail of the collision LOD of ships, higher values are more detailed but heavier to compute"
    )
    var lodDetail = 4096

    @JsonSchema(
        description = "Sets number of sub-steps used by Krunch"
    )
    var subSteps = 20

    @JsonSchema(
        description = "Sets number of iterations per sub-steps used by Krunch"
    )
    var iterations = 2

    @JsonSchema(
        description = "Sets the constraint solver used by Krunch"
    )
    var solver: SolverType = SolverType.GAUSS_SEIDEL

    @JsonSchema(
        description = "Limit the max collision de-penetration speed so that rigid bodies don't go flying apart when they overlap"
    )
    var maxDePenetrationSpeed = 10.0
}

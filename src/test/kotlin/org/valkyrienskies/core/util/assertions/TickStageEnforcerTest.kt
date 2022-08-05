package org.valkyrienskies.core.util.assertions

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import org.valkyrienskies.core.util.assertions.stages.TickStageEnforcer
import org.valkyrienskies.core.util.assertions.stages.constraints.ConstraintFailedException
import org.valkyrienskies.core.util.assertions.stages.constraints.StageConstraint
import org.valkyrienskies.core.util.assertions.stages.predicates.StagePredicate
import java.util.concurrent.Executors

class TickStageEnforcerTest : StringSpec({

    "requires first stage is a reset" {
        val enforcer = TickStageEnforcer("b")

        shouldThrow<IllegalArgumentException> {
            enforcer.stage("a")
        }
    }

    "enforces order" {
        val enforcer = TickStageEnforcer("a", StageConstraint.requireOrder("a", "b", "c"))

        enforcer.stage("a")
        enforcer.stage("d")

        shouldThrow<ConstraintFailedException> {
            enforcer.stage("c")
        }
    }

    "enforces predicate ordering with oneof" {
        val enforcer = TickStageEnforcer(
            "a",
            StageConstraint.requireOrder(
                StagePredicate.single("a"),
                StagePredicate.oneOf("b", "c", "d"),
                StagePredicate.single("f")
            )
        )

        enforcer.stage("a")
        enforcer.stage("b")
        enforcer.stage("d")
        enforcer.stage("c")
        enforcer.stage("b")

        enforcer.stage("a")
        enforcer.stage("f")

        shouldThrow<ConstraintFailedException> {
            enforcer.stage("b")
        }
    }

    "enforces predicate ordering with oneof (builder style)" {
        val enforcer = TickStageEnforcer("a") {
            requireOrder {
                single("a")
                oneOf("b", "c", "d")
                single("f")
            }
        }

        enforcer.stage("a")
        enforcer.stage("b")
        enforcer.stage("d")
        enforcer.stage("c")
        enforcer.stage("b")

        enforcer.stage("a")
        enforcer.stage("f")

        shouldThrow<ConstraintFailedException> {
            enforcer.stage("b")
        }
    }

    "allows duplicates in order" {
        val enforcer = TickStageEnforcer("a", StageConstraint.requireOrder("a", "b", "c"))

        enforcer.stage("a")
        enforcer.stage("b")
        enforcer.stage("b")
        enforcer.stage("c")
        enforcer.stage("c")
        enforcer.stage("a")
    }

    "enforces required stages" {
        val enforcer = TickStageEnforcer("a", StageConstraint.requireStages("a", "b", "c"))

        enforcer.stage("a")
        enforcer.stage("b")
        enforcer.stage("b")

        shouldThrow<ConstraintFailedException> {
            enforcer.stage("a")
        }

        enforcer.stage("b")
        enforcer.stage("c")
        enforcer.stage("a")
    }

    "enforces required stages and order" {
        val enforcer = TickStageEnforcer("a", StageConstraint.requireStagesAndOrder("a", "b", "c"))

        enforcer.stage("a")
        enforcer.stage("b")
        enforcer.stage("0")
        enforcer.stage("1")

        shouldThrow<ConstraintFailedException> {
            enforcer.stage("a")
        }

        enforcer.stage("1")
        enforcer.stage("b")
        enforcer.stage("c")
        enforcer.stage("a")
    }

    "allows full cycle of correct oder" {
        val enforcer = TickStageEnforcer("a", StageConstraint.requireStagesAndOrder("a", "b", "c"))

        repeat(3) {
            enforcer.stage("a")
            enforcer.stage("b")
            enforcer.stage("c")
        }
    }

    "enforces correct thread" {
        val thread = Thread()
        val executor = Executors.newSingleThreadExecutor { thread }

        val enforcer = TickStageEnforcer("a", StageConstraint.requireThread({ it == thread }, "b"))

        enforcer.stage("a")

        shouldThrow<ConstraintFailedException> {
            enforcer.stage("b")
        }

        executor.submit {
            shouldNotThrow<ConstraintFailedException> {
                enforcer.stage("b")
            }
        }
    }

})

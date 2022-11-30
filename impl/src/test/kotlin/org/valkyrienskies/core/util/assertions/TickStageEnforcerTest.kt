package org.valkyrienskies.core.util.assertions

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import org.valkyrienskies.core.util.assertions.stages.ConstraintFailedException
import org.valkyrienskies.core.util.assertions.stages.TickStageEnforcer
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
        val enforcer = TickStageEnforcer("a") {
            requireOrder("a", "b", "c")
        }

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
                StagePredicate.anyOf("b", "c", "d"),
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
                anyOf("b", "c", "d")
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

    "enforces no duplicates" {
        val enforcer = TickStageEnforcer("a") {
            requireNoDuplicates("c", "d")
        }

        enforcer.stage("a")
        enforcer.stage("b")
        enforcer.stage("c")
        enforcer.stage("b")
        enforcer.stage("b")

        shouldThrow<ConstraintFailedException> {
            enforcer.stage("c")
        }

        enforcer.stage("a")
    }

    "enforces required final stage" {
        val enforcer = TickStageEnforcer("a") {
            requireFinal("z")
        }

        enforcer.stage("a")
        enforcer.stage("b")
        enforcer.stage("c")

        shouldThrow<ConstraintFailedException> {
            enforcer.stage("a")
        }

        enforcer.stage("z")
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

    "require exact order" {
        val enforcer = TickStageEnforcer("a") {
            requireExactOrder("a", "b", "c")
        }

        repeat(2) {
            enforcer.stage("a")
            enforcer.stage("b")
            shouldThrow<ConstraintFailedException> {
                enforcer.stage("d")
            }
        }
    }

    "ignores until first reset" {
        val enforcer = TickStageEnforcer("a") {
            ignoreUntilFirstReset()

            requireExactOrder("a", "b", "c")
        }

        repeat(3) {
            enforcer.stage("b")
            enforcer.stage("d")
        }

        enforcer.stage("a")
        enforcer.stage("b")

        shouldThrow<ConstraintFailedException> {
            enforcer.stage("d")
        }
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

    "require multiple orders (predicate)" {
        val enforcer = TickStageEnforcer("a") {
            requireOrder {
                single("a")
                single("g")
                single("z")
            }

            requireOrder {
                single("a")
                single("c")
            }
        }

        enforcer.stage("a")
        enforcer.stage("z")
        shouldThrow<ConstraintFailedException> {
            enforcer.stage("g")
        }

        enforcer.stage("a")
        enforcer.stage("b")
        enforcer.stage("g")
        enforcer.stage("c")
        enforcer.stage("z")
    }

})

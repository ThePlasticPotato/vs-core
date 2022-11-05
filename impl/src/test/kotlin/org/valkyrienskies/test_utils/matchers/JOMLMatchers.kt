package org.valkyrienskies.test_utils.matchers

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import org.joml.Vector3d

fun equalWithDelta(other: Vector3d, delta: Double) = Matcher<Vector3d> {
    MatcherResult(
        it.equals(other, delta),
        { "Vector3d was $it but we expected it to equal $other with a delta of $delta" },
        { "Vector3d $it should not equal $other with a delta of $delta" },
    )
}

fun Vector3d.shouldBeWithinDelta(other: Vector3d, delta: Double) =
    also { this should equalWithDelta(other, delta) }

fun Vector3d.shouldNotBeWithinDelta(other: Vector3d, delta: Double) =
    also { this shouldNot equalWithDelta(other, delta) }

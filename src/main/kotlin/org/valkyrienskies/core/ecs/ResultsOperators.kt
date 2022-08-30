package org.valkyrienskies.core.ecs

import dev.dominion.ecs.api.Results

operator fun <T> Results.With1<T>.component1() = comp!!
operator fun <T1, T2> Results.With2<T1, T2>.component1() = comp1!!
operator fun <T1, T2> Results.With2<T1, T2>.component2() = comp2!!
operator fun <T1, T2, T3> Results.With3<T1, T2, T3>.component1() = comp1!!
operator fun <T1, T2, T3> Results.With3<T1, T2, T3>.component2() = comp2!!
operator fun <T1, T2, T3> Results.With3<T1, T2, T3>.component3() = comp3!!

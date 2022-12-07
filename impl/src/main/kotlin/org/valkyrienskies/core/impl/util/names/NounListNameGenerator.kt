package org.valkyrienskies.core.impl.util.names

import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors

/**
 * Generates names from a noun list
 */
object NounListNameGenerator : NameGenerator {
    private const val NOUN_LIST_LENGTH = 6801
    private const val DEFAULT_NOUNS_PER_NAME = 3

    private val nouns: List<String> = javaClass
        .classLoader!!
        .getResourceAsStream("nounlist.txt")!!
        .bufferedReader()
        .lines()
        .collect(Collectors.toList()) // This doesn't use `.toList()` because it breaks java 8 support

    override fun generateName(): String {
        return this.generateName(DEFAULT_NOUNS_PER_NAME)
    }

    fun generateName(numberOfNouns: Int): String {
        return ThreadLocalRandom.current()
            .ints(numberOfNouns.toLong(), 0, nouns.size)
            .mapToObj { index -> nouns[index] }
            .collect(Collectors.joining("-"))
    }
}

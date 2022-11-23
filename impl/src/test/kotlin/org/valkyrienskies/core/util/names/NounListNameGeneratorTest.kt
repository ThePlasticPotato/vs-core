package org.valkyrienskies.core.util.names

import org.junit.jupiter.api.Test

class NounListNameGeneratorTest {

    /**
     * Tests that we can generate a noun, which for some reason this has issues when running on java 8.
     */
    @Test
    fun testGenerateNoun() {
        val name = NounListNameGenerator.generateName()
    }
}

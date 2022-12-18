package org.valkyrienskies.test_utils

// https://stackoverflow.com/questions/42739807/how-to-read-a-text-file-from-resources-in-kotlin
fun getResourceAsBytes(path: String): ByteArray? =
    object {}.javaClass.getResource(path)?.readBytes()

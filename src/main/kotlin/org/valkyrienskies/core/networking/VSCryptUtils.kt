package org.valkyrienskies.core.networking

import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

internal object VSCryptUtils {
    fun generateAES128Key(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(128)
        return keyGenerator.generateKey()
    }
}

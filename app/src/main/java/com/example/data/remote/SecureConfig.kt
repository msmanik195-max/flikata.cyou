package com.example.data.remote

/**
 * Secure Endpoint Guardian & Runtime Obfuscation Layer
 * 
 * Protects backend URLs, server hostnames, and API endpoints from
 * static reverse engineering, APK deconstruction, JADX/DEX string dumping,
 * and binary analysis.
 */
object SecureConfig {

    // Cryptographically scrambled, non-sequential multi-part payloads
    private val PART_ALPHA = intArrayOf(
        147, 175, 138, 127, 241, 160, 224, 137, 218, 138, 62, 145, 42, 157, 6
    )
    private val PART_BETA = intArrayOf(
        255, 216, 39, 138, 223, 11, 205, 147, 42, 13, 223, 62, 6, 100
    )

    // Dynamic runtime key synthesis defeating static constant analysis
    private fun getDynamicKey(index: Int): Int {
        val keys = intArrayOf(
            (0x2D shl 1) xor 0x00, // 0x5A
            (0x1F shl 1) or 0x01,   // 0x3F
            (0x46 shl 1) xor 0x00, // 0x8C
            (0x09 shl 1) xor 0x00, // 0x12
            (0x3F shl 1) xor 0x00, // 0x7E
            (0x26 shl 1) or 0x01,   // 0x4D
            (0x49 shl 1) or 0x01,   // 0x93
            (0x15 shl 1) or 0x01    // 0x2B
        )
        return keys[index % keys.size]
    }

    /**
     * Runtime byte de-obfuscation pipeline with dual-stage bitwise arithmetic
     */
    private fun resolveEndpoint(): String {
        val totalLength = PART_ALPHA.size + PART_BETA.size
        val output = ByteArray(totalLength)

        for (i in 0 until totalLength) {
            val raw = if (i % 2 == 0) {
                PART_ALPHA[i / 2]
            } else {
                PART_BETA[i / 2]
            }

            val key = getDynamicKey(i)
            val unshifted = ((raw xor 0xAA) - (i * 3 + 7)) and 0xFF
            val charCode = (unshifted xor key) and 0xFF
            output[i] = charCode.toByte()
        }

        return String(output, Charsets.UTF_8)
    }

    // Cached decrypted endpoint
    val baseUrl: String by lazy {
        resolveEndpoint()
    }

    val baseDomain: String by lazy {
        val url = baseUrl.trimEnd('/')
        if (url.endsWith("/backend")) {
            url.removeSuffix("/backend")
        } else {
            url
        }
    }

    fun getProfileUrl(userId: String?): String {
        val clean = userId?.removePrefix("user_")?.trim() ?: ""
        return if (clean.isNotBlank()) {
            "${baseUrl}profile.php?id=$clean"
        } else {
            "${baseUrl}profile.php"
        }
    }

    fun getPostShareUrl(targetId: String, isReel: Boolean): String {
        val clean = targetId.removePrefix("post_").removePrefix("reel_").trim()
        val param = if (isReel) "reel_id" else "id"
        return "${baseUrl}post.php?$param=$clean"
    }

    fun normalizeUrl(rawUrl: String): String {
        if (rawUrl.isBlank()) return ""
        val trimmed = rawUrl.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed.replace("http://", "https://")
        }
        val cleanPath = trimmed.removePrefix("/")
        return "$baseDomain/$cleanPath"
    }
}

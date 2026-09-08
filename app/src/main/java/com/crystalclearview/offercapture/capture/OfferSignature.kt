package com.crystalclearview.offercapture.capture

import java.security.MessageDigest

object OfferSignature {
    fun normalize(rawText: String): String {
        return rawText
            .lowercase()
            .replace(Regex("\\$\\s*\\d+(?:\\.\\d{1,2})?"), "$")
            .replace(Regex("\\b\\d+(?:\\.\\d+)?\\s*(?:mi|mile|miles)\\b"), "MILES")
            .replace(Regex("\\b\\d+\\s*(?:min|mins|minute|minutes)\\b"), "MINUTES")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun hash(rawText: String): String {
        val normalized = normalize(rawText)
        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

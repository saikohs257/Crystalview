package com.crystalclearview.offercapture.capture

object PlatformRegistry {
    private val packageToPlatform = mapOf(
        "com.uber.driver" to "UBER",
        "com.ubercab.driver" to "UBER",
        "com.dd.doordash" to "DOORDASH",
        "com.doordash.driverapp" to "DOORDASH"
    )

    fun platformForPackage(packageName: String): String? = packageToPlatform[packageName]

    fun isSupported(packageName: String): Boolean = packageName in packageToPlatform
}

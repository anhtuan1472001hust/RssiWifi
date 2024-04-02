package com.example.rssiwifi

data class WifiModel(
    val wifiName: String? = null,
    val wifiMac: String? = null,
    val wifiRssi: Int? = null,
    var selected: Boolean = false,
)

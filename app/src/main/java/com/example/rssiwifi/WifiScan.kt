package com.example.rssiwifi

data class WifiScan(
    val listWifi: List<WifiModel>,
    val longitude: String,
    val latitude: String,
)

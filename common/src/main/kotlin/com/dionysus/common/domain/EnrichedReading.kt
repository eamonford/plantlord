package com.dionysus.common.domain

data class EnrichedReading(private val baseReading: Reading,
                           val sensorName: String,
                           val type: String,
                           val plantId: Int,
                           val timestamp: Long = System.currentTimeMillis()) {

    companion object

    val deviceId: String by lazy { baseReading.deviceId }
    val value: Int by lazy { baseReading.value }
    val battery: Double by lazy { baseReading.battery }
}
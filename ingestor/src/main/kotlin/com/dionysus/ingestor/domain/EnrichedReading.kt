package com.dionysus.ingestor.domain

import com.dionysus.common.domain.Reading

data class EnrichedReading(private val baseReading: Reading, val sensorName: String, val type: String) {
    val deviceId: String by lazy {baseReading.deviceId }
    val value: Int by lazy {baseReading.value}
    val battery: Double by lazy {baseReading.battery}
}
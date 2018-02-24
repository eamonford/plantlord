package com.dionysus.ingestor.domain

import com.dionysus.common.domain.Reading

data class EnrichedReading(private val baseReading: Reading, val sensorName: String) {
    val deviceId: String by lazy {baseReading.deviceId}
    val value: Double by lazy {baseReading.value}
}
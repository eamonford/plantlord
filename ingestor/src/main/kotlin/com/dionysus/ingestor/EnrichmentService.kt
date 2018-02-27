package com.dionysus.ingestor

import com.dionysus.common.domain.Reading
import com.dionysus.common.domain.SensorsService
import com.dionysus.ingestor.domain.EnrichedReading
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map

class EnrichmentService(private val sensorsService: SensorsService) {

    fun enrichReading(reading: Reading): Result<EnrichedReading, Throwable> =
         sensorsService
                .getSensorByDeviceId(reading.deviceId)
                .map { EnrichedReading(baseReading = reading, sensorName = it.name, type = it.type) }
}
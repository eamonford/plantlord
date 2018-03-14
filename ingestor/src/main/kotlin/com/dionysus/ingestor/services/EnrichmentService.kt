package com.dionysus.ingestor.services

import com.dionysus.common.domain.EnrichedEvent
import com.dionysus.common.domain.Reading
import com.dionysus.common.services.SensorsService
import com.dionysus.common.domain.EnrichedReading
import com.dionysus.common.domain.Event
import com.dionysus.common.services.ValvesService
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map

open class EnrichmentService(private val sensorsService: SensorsService, private val valvesService: ValvesService) {

    open fun enrichReading(reading: Reading): Result<EnrichedReading, Throwable> =
            sensorsService
                    .getSensorByDeviceId(reading.deviceId)
                    .map { EnrichedReading(baseReading = reading, sensorName = it.name, type = it.type, plantId = it.plantId) }

    open fun enrichEvent(event: Event): Result<EnrichedEvent, Throwable> =
            valvesService
                    .getByHardwareId(event.valveId)
                    .map { EnrichedEvent(baseEvent = event, plantId = it.plantId) }
}
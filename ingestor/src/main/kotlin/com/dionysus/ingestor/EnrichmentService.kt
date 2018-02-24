package com.dionysus.ingestor

import com.dionysus.common.domain.Reading
import com.dionysus.ingestor.domain.EnrichedReading
import com.dionysus.irrigator.dao.RuleRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map

class EnrichmentService(private val ruleRepository: RuleRepository) {

    fun enrichReading(reading: Reading): Result<EnrichedReading, Throwable> =
         ruleRepository
                .getBySensorId(reading.deviceId)
                .map { EnrichedReading(baseReading = reading, sensorName = "some sensor name") }
}
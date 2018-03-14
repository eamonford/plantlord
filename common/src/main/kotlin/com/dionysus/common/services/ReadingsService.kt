package com.dionysus.common.services

import com.dionysus.common.dao.InfluxDAO
import com.dionysus.common.domain.EnrichedReading
import com.dionysus.common.domain.Reading
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import org.influxdb.dto.Point
import java.time.Instant
import java.util.concurrent.TimeUnit

open class ReadingsService(private val influxDAO: InfluxDAO) {

    open fun write(reading: EnrichedReading): Result<EnrichedReading, Throwable> =
            Result.of {
                influxDAO.write(reading.toPoint())
                influxDAO.write(reading.toPointForBattery())
                reading
            }

    fun findReadings(dateRange: LongRange? = null, plantId: Int? = null): Result<List<EnrichedReading>, Throwable> =
            influxDAO.find(
                    measurement = "moisture",
                    dateRange = dateRange,
                    tagMatchers = if (plantId != null) hashMapOf(Pair("plantId", plantId.toString())) else null
            ).flatMap {
                Result.of { it.map { EnrichedReading.fromInfluxQueryResult(it) } }
            }
}

fun EnrichedReading.toPoint(): Point =
        Point
                .measurement(type)
                .time(timestamp, TimeUnit.MILLISECONDS)
                .addField("value", value)
                .tag("sensorName", sensorName)
                .tag("deviceId", deviceId)
                .tag("plantId", plantId.toString())
                .build()

fun EnrichedReading.toPointForBattery(): Point =
        Point
                .measurement("battery")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", battery)
                .tag("sensorName", sensorName)
                .tag("deviceId", deviceId)
                .build()

fun EnrichedReading.Companion.fromInfluxQueryResult(queryResult: Map<String, Any?>): EnrichedReading =
        EnrichedReading(
                baseReading = Reading(
                        deviceId = queryResult["deviceId"] as String,
                        battery = 0.0,
                        value = (queryResult["value"] as Double).toInt()),
                sensorName = queryResult["sensorName"] as String,
                plantId = if (queryResult["plantId"] != null) (queryResult["plantId"] as String).toInt() else 0,
                type = "moisture",
                timestamp = Instant.parse(queryResult["time"] as String).toEpochMilli())

package com.dionysus.ingestor.dao

import com.dionysus.common.domain.Event
import com.dionysus.ingestor.domain.EnrichedReading
import com.dionysus.common.exceptions.DionysusConnectionException
import com.github.michaelbull.result.Result
import mu.KotlinLogging
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

fun EnrichedReading.toPoint(): Point =
        Point
                .measurement(type)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", value)
                .tag("sensorName", sensorName)
                .tag("deviceId", deviceId)
                .build()

fun EnrichedReading.toPointForBattery(): Point =
        Point
                .measurement("battery")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", battery)
                .tag("sensorName", sensorName)
                .tag("deviceId", deviceId)
                .build()

fun Event.toPoint(): Point =
        Point
                .measurement("events")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", value)
                .tag("valveId", valveId.toString())
                .build()


open class InfluxDAO private constructor(private val influxDB: InfluxDB) {

    companion object {
        fun connect(url: String, username: String, password: String): InfluxDAO {
            val influxDB: InfluxDB = InfluxDBFactory.connect(url, username, password).setDatabase("dionysus")
            val dao = InfluxDAO(influxDB).testConnection(url)
            logger.info { "Connected to InfluxDB at $url" }
            return dao
        }
    }

    open fun writeReading(reading: EnrichedReading): Result<EnrichedReading, Throwable> =
            Result.of {
                influxDB.write(reading.toPoint())
                influxDB.write(reading.toPointForBattery())
                reading
            }


    open fun writeEvent(event: Event): Result<Event, Throwable> = Result.of { influxDB.write(event.toPoint()); event }

    fun testConnection(url: String): InfluxDAO {
        try {
            influxDB.ping().version.startsWith("v", ignoreCase = true)
        } catch (e: Throwable) {
            throw DionysusConnectionException("Could not connect to InfluxDB at $url", e)
        }
        return this
    }
}
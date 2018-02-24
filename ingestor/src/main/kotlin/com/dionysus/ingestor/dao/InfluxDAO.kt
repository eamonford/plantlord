package com.dionysus.ingestor.dao

import com.dionysus.common.domain.Event
import com.dionysus.ingestor.domain.EnrichedReading
import com.github.michaelbull.result.Result
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit


fun EnrichedReading.toPoint(): Point =
        Point
                .measurement("readings")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", value)
                .addField("sensorName", sensorName)
                .tag("deviceId", deviceId)
                .build()

fun Event.toPoint(): Point =
        Point
                .measurement("events")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", value)
                .tag("valveId", valveId)
                .build()


open class InfluxDAO(url: String, username: String, password: String) {

    private val influxDB: InfluxDB = InfluxDBFactory.connect(url, username, password).setDatabase("dionysus")

    init {
        testConnection(influxDB)
    }

    open fun writeReading(reading: EnrichedReading): Result<EnrichedReading, Throwable> = Result.of { influxDB.write(reading.toPoint()); reading }
    open fun writeEvent(event: Event): Result<Event, Throwable> = Result.of { influxDB.write(event.toPoint()); event }


//    influxDB.close()

    fun testConnection(db: InfluxDB) {
        val response = db.ping()
        if (response.version.equals("unknown", ignoreCase = true)) {
            println("Error pinging server.")
            return
        } else {
            println("Connected to InfluxDB")
        }
    }
}
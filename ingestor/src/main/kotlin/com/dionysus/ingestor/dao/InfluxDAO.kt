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
                .measurement(type)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", value)
                .addField("sensorName", sensorName)
                .tag("deviceId", deviceId)
                .build()

fun EnrichedReading.toPointForBattery(): Point =
        Point
                .measurement("battery")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", battery)
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


open class InfluxDAO private constructor(private val influxDB: InfluxDB) {

    companion object {
        fun connect(url: String, username: String, password: String): InfluxDAO {
            val influxDB: InfluxDB = InfluxDBFactory.connect(url, username, password).setDatabase("dionysus")
            val dao = InfluxDAO(influxDB)
            if (dao.canConnect())
                return dao
            throw Exception("can't connect to influx")
        }
    }

    open fun writeReading(reading: EnrichedReading): Result<EnrichedReading, Throwable> =
            Result.of {
                influxDB.write(reading.toPoint())
                influxDB.write(reading.toPointForBattery())
                reading
            }


    open fun writeEvent(event: Event): Result<Event, Throwable> = Result.of { influxDB.write(event.toPoint()); event }

    private fun canConnect(): Boolean =
            influxDB.ping().version.equals("unknown", ignoreCase = true)

}
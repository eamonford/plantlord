package com.dionysus.ingestor

import com.dionysus.common.domain.Event
import com.dionysus.common.domain.Reading
import com.github.michaelbull.result.Result
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit


fun Reading.toPoint(): Point =
        Point
                .measurement("readings")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", value)
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

    private val influxDB: InfluxDB = InfluxDBFactory.connect(url, username, password)

    open fun writeReading(reading: Reading): Result<Reading, Throwable> = Result.of { influxDB.write(reading.toPoint()); reading }
    open fun writeEvent(event: Event): Result<Event, Throwable> = Result.of { influxDB.write(event.toPoint()); event }


//    influxDB.close()

}
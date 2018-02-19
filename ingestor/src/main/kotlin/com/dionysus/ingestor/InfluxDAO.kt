package com.dionysus.ingestor

import com.dionysus.common.domain.Event
import com.dionysus.common.domain.Reading
import com.github.michaelbull.result.Result
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point


fun Reading.toPoint(): Point =
    Point
            .measurement("")
            .build()

fun Event.toPoint(): Point =
        Point
                .measurement("")
                .build()


class InfluxDAO(url: String, username: String, password: String) {

    private val influxDB: InfluxDB = InfluxDBFactory.connect(url, username, password)

    fun writeReading(reading: Reading): Result<Reading, Throwable> = Result.of { influxDB.write(reading.toPoint()); reading }
    fun writeEvent(event: Event): Result<Event, Throwable> = Result.of { influxDB.write(event.toPoint()); event }


//    influxDB.close()

}
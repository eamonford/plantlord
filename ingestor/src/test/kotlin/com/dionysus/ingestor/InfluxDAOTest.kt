package com.dionysus.ingestor

import com.dionysus.common.domain.Reading
import org.influxdb.dto.Point
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals

class InfluxDAOTest : Spek({
//    given("a Reading") {
//        val reading = Reading("deviceId", 10.0)
//        on("calling toPoint()") {
//            val point = reading.toPoint()
//            it("should return a valid InfluxDB Point") {
//                val expectedPoint = Point
//                        .measurement("readings")
//                        .addField("value", 10.0)
//                        .tag("deviceId", "deviceId")
//                        .build()
//                assertEquals(expectedPoint, point)
//            }
//        }
//    }
})
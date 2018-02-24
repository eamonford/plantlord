//package com.dionysus.ingestor.domain
//
//import com.dionysus.common.domain.Reading
//import org.jetbrains.spek.api.Spek
//import org.jetbrains.spek.api.dsl.given
//import org.jetbrains.spek.api.dsl.it
//import org.jetbrains.spek.api.dsl.on
//
//class EnrichedReadingTest : Spek({
//    given("an EnrichedReading") {
//        val baseReading = Reading("test", 50, 10.0)
//        val enrichedReading = EnrichedReading(baseReading)
//        println("enrichedReading.deviceId: ${enrichedReading.deviceId}")
//
//        enrichedReading.baseReading.deviceId = "second test"
//        on("getting a property") {
//            it("should return the delgated property") {
//                println("baseReading.deviceId: ${enrichedReading.baseReading.deviceId}")
//                println("enrichedReading.deviceId: ${enrichedReading.deviceId}")
////                assertEquals("second test", enrichedReading.deviceId)
//            }
//        }
//    }
//})
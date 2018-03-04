package com.dionysus.ingestor

import com.dionysus.common.domain.Event
import com.dionysus.common.domain.Reading
import com.dionysus.common.exceptions.DionysusParseException
import com.dionysus.ingestor.dao.InfluxDAO
import com.dionysus.ingestor.domain.EnrichedReading
import com.dionysus.ingestor.services.EnrichmentService
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.nhaarman.mockito_kotlin.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertTrue


class IngestionControllerTest : Spek({

    given("the IngestionController, with other services mocked for Readings") {
        val enrichedReading = EnrichedReading(
                baseReading = Reading("test", 50.0, 1),
                sensorName = "testSensor",
                type = "moisture")
        val mockInfluxDAO = mock<InfluxDAO> {
            on { writeReading(any()) }.doReturn(Ok(enrichedReading))
        }
        val mockEnrichmentService = mock<EnrichmentService> {
            on { enrichReading(any()) }.doReturn(Ok(enrichedReading))
        }
        val ingestionController = IngestionController(mockInfluxDAO, mockEnrichmentService)

        on("parsing a valid message") {
            val message = MqttMessage("""{"device_id": "deviceId", "value": 10, "battery": 100.0}""".toByteArray())
            val result = ingestionController.processReadingsTopic(message)
            it("should call the InfluxDAO and return an EnrichedReading") {
                verify(mockEnrichmentService).enrichReading(any())
                verify(mockInfluxDAO).writeReading(any())
                assertTrue { result is Ok }
            }
        }
        on("trying to parse an unparsable messgae") {
            val message = MqttMessage("""a bad message""".toByteArray())
            val result = ingestionController.processReadingsTopic(message)
            it("should fail with an error") {
                verifyZeroInteractions(mockEnrichmentService)
                verifyZeroInteractions(mockInfluxDAO)
                assertTrue { result is Err }
                assertTrue { result.getError() is DionysusParseException }
            }
        }
    }

    given("the IngestionController, with other services mocked for Events") {
        val mockInfluxDAO = mock<InfluxDAO> {
            on { writeEvent(any()) }.doAnswer { invocationOnMock -> Ok(invocationOnMock.getArgument<Event>(0)) }
        }

        val ingestionController = IngestionController(mockInfluxDAO, mock())

        on("parsing a valid message") {
            val message = MqttMessage("""{"valveId": 123, "value": 10.0}""".toByteArray())
            val result = ingestionController.processEventsTopic(message)
            it("should call the InfluxDAO and return an Event") {
                verify(mockInfluxDAO).writeEvent(any())
                assertTrue { result is Ok }
            }
        }
        on("trying to parse an unparsable messgae") {
            val message = MqttMessage("""a bad message""".toByteArray())
            val result = ingestionController.processEventsTopic(message)
            it("should fail with an error") {
                verifyZeroInteractions(mockInfluxDAO)
                assertTrue { result is Err }
                assertTrue { result.getError() is DionysusParseException }
            }
        }
    }
})
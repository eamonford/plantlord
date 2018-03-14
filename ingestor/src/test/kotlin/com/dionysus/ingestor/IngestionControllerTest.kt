package com.dionysus.ingestor

import com.dionysus.common.domain.EnrichedEvent
import com.dionysus.common.domain.EnrichedReading
import com.dionysus.common.domain.Event
import com.dionysus.common.domain.Reading
import com.dionysus.common.exceptions.DionysusParseException
import com.dionysus.common.dao.InfluxDAO
import com.dionysus.common.services.EventsService
import com.dionysus.common.services.ReadingsService
import com.dionysus.ingestor.services.EnrichmentService
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.nhaarman.mockito_kotlin.*
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertTrue

class IngestionControllerTest : Spek({

    given("the IngestionController, with other services mocked") {
        val enrichedReading = EnrichedReading(
                baseReading = Reading("test", 50.0, 1),
                sensorName = "testSensor",
                type = "moisture",
                plantId = 1)
        val enrichedEvent = EnrichedEvent(baseEvent = Event(123, 1), plantId = 1)
        val mockReadingsService = mock<ReadingsService> {
            on { write(any()) }.doReturn(Ok(enrichedReading))
        }
        val mockEventsService = mock<EventsService> {
            on { write(any()) }.doReturn(Ok(enrichedEvent))
        }
        val mockEnrichmentService = mock<EnrichmentService> {
            on { enrichReading(any()) }.doReturn(Ok(enrichedReading))
            on { enrichEvent(any()) }.doReturn(Ok(enrichedEvent))
        }
        val mockMqttClient = mock<MqttAsyncClient> {
            on { connect() }.doReturn(mock<IMqttToken>())
            on { isConnected }.doReturn(true)
        }
        val ingestionController = IngestionController(mockEventsService, mockReadingsService, mockEnrichmentService, mockMqttClient)

        on("parsing a valid message") {
            val message = MqttMessage("""{"deviceId": "deviceId", "value": 10, "battery": 100.0}""".toByteArray())
            val result = ingestionController.processReadingsTopic(message)
            it("should invoke the EnrichmentService") { verify(mockEnrichmentService).enrichReading(any()) }
            it("should invoke the ReadingsService") { verify(mockReadingsService).write(any()) }
            it("should return an EnrichedReading") { assertTrue { result is Ok } }
        }
        on("trying to parse an unparsable message") {
            val message = MqttMessage("""a bad message""".toByteArray())
            val result = ingestionController.processReadingsTopic(message)
            it("should fail with an error") {
                verifyZeroInteractions(mockEnrichmentService)
                verifyZeroInteractions(mockReadingsService)
                assertTrue { result is Err }
                assertTrue { result.getError() is DionysusParseException }
            }
        }
        on("parsing a valid message") {
            val message = MqttMessage("""{"valveId": 123, "value": 10}""".toByteArray())
            val result = ingestionController.processEventsTopic(message)
            it("should invoke the EventsService") { verify(mockEventsService).write(any()) }
            it("should return an Event") { assertTrue { result is Ok } }
        }
        on("trying to parse an unparsable messgae") {
            val message = MqttMessage("""a bad message""".toByteArray())
            val result = ingestionController.processEventsTopic(message)
            it("should fail with an error") {
                verifyZeroInteractions(mockReadingsService)
                assertTrue { result is Err }
                assertTrue { result.getError() is DionysusParseException }
            }
        }
    }
})
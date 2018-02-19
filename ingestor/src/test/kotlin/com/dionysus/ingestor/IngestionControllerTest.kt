package com.dionysus.ingestor

import com.dionysus.common.domain.Reading
import com.github.michaelbull.result.Ok
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on


class IngestionControllerTest : Spek({


    given("the readings topic and a json message") {
        val mockInfluxDAO = mock<InfluxDAO> {
            on { writeReading(any()) }.doReturn(Ok(Reading("test", 1.0)))
        }
        val ingestionController = IngestionController(mockInfluxDAO)
        val topic = "dionysus/readings"
        val message = MqttMessage()
        message.payload = """{"deviceId": "deviceId", "value": 10.0}""".toByteArray()
        on("trying to parse the message") {
            ingestionController.messageArrived(topic, message)
            it("should write the reading to Influx") {
                verify(mockInfluxDAO).writeReading(any())
            }
        }
    }
})
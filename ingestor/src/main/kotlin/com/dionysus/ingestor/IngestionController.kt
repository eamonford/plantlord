package com.dionysus.ingestor

import com.beust.klaxon.Klaxon
import com.dionysus.common.ENRICHED_READINGS_TOPIC
import com.dionysus.common.EVENTS_TOPIC
import com.dionysus.common.READINGS_TOPIC
import com.dionysus.common.domain.Event
import com.dionysus.common.domain.Reading
import com.dionysus.common.exceptions.DionysusConnectionException
import com.dionysus.common.exceptions.DionysusParseException
import com.dionysus.ingestor.dao.InfluxDAO
import com.dionysus.ingestor.domain.EnrichedReading
import com.dionysus.ingestor.services.EnrichmentService
import com.github.michaelbull.result.*
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

private val logger = KotlinLogging.logger {}

class IngestionController(private val influxDAO: InfluxDAO,
                          private val enrichmentService: EnrichmentService,
                          private val mqttUrl: String,
                          mqttClientId: String) : MqttCallback {

    private val mqttClient = MqttAsyncClient(mqttUrl, mqttClientId)

    init {
        mqttClient.setCallback(this)
        connectToMqtt()
    }

    private fun connectToMqtt() {
        try {
            while (!mqttClient.isConnected) mqttClient.connect().waitForCompletion()

            logger.info { "Connected to MQTT at $mqttUrl" }
            mqttClient.subscribe(READINGS_TOPIC, 0)
            mqttClient.subscribe(EVENTS_TOPIC, 0)
        } catch (e: Throwable) {
            throw DionysusConnectionException("Connection to MQTT failed for $mqttUrl", e)
        }
    }

    fun processReadingsTopic(message: MqttMessage?): Result<EnrichedReading, Throwable> =
            message
                    .toString()
                    .toResultOr { DionysusParseException("The reading was an empty string") }
                    .flatMap { Result.of { Klaxon().parse<Reading>(it)!! } }
                    .mapError { e -> DionysusParseException("Could not parse message: ${message.toString()}", e) }
                    .flatMap { enrichmentService.enrichReading(it) }
                    .flatMap { influxDAO.writeReading(it) }
                    .flatMap { publishEnrichedReading(it) }

    fun processEventsTopic(message: MqttMessage?) =
            message
                    .toString()
                    .toResultOr { DionysusParseException("The event was an empty string") }
                    .flatMap { Result.of { Klaxon().parse<Event>(it)!! } }
                    .mapError { e -> DionysusParseException("Could not parse message: ${message.toString()}", e) }
                    .flatMap { influxDAO.writeEvent(it) }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        when (topic) {
            READINGS_TOPIC ->
                processReadingsTopic(message).mapBoth(
                        success = { logger.info { "Wrote reading to database for sensor ${it.sensorName}" } },
                        failure = { logger.error("Failed to write reading to database.", it) })
            EVENTS_TOPIC -> processEventsTopic(message).mapBoth(
                    success = { logger.info { "Wrote event to database for valve ID ${it.valveId}" } },
                    failure = { logger.error("Failed to write event to database.", it) })
        }
    }

    private fun publishEnrichedReading(enrichedReading: EnrichedReading): Result<EnrichedReading, Throwable> =
            Result.of {
                mqttClient.publish(
                        ENRICHED_READINGS_TOPIC,
                        MqttMessage(Klaxon().toJsonString(enrichedReading).toByteArray()))
                enrichedReading
            }

    override fun connectionLost(cause: Throwable?) {
        logger.error ( "Lost MQTT connection. Will attempt to reconnect...", cause )
        connectToMqtt()
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        logger.info { "Published message to topic ${token?.topics?.first()}" }
    }
}
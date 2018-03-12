package com.dionysus.ingestor

import com.beust.klaxon.Klaxon
import com.dionysus.common.EVENTS_TOPIC
import com.dionysus.common.READINGS_TOPIC
import com.dionysus.common.domain.Event
import com.dionysus.common.domain.Reading
import com.dionysus.common.exceptions.DionysusParseException
import com.dionysus.ingestor.dao.InfluxDAO
import com.dionysus.ingestor.domain.EnrichedReading
import com.dionysus.ingestor.services.EnrichmentService
import com.github.michaelbull.result.*
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

private val logger = KotlinLogging.logger {}

class IngestionController(private val influxDAO: InfluxDAO, private val enrichmentService: EnrichmentService) : MqttCallback {

    fun processReadingsTopic(message: MqttMessage?): Result<EnrichedReading, Throwable> =
            message
                    .toString()
                    .toResultOr { DionysusParseException("The reading was an empty string") }
                    .flatMap { Result.of { Klaxon().parse<Reading>(it)!! } }
                    .mapError { e -> DionysusParseException("Could not parse message: ${message.toString()}", e) }
                    .flatMap { enrichmentService.enrichReading(it) }
                    .flatMap { influxDAO.writeReading(it) }

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

    override fun connectionLost(cause: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
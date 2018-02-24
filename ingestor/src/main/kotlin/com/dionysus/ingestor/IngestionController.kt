package com.dionysus.ingestor

import com.beust.klaxon.Klaxon
import com.dionysus.common.domain.EVENTS_TOPIC
import com.dionysus.common.domain.Event
import com.dionysus.common.domain.READINGS_TOPIC
import com.dionysus.common.domain.Reading
import com.dionysus.ingestor.dao.InfluxDAO
import com.github.michaelbull.result.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

class IngestionController(private val influxDAO: InfluxDAO, private val enrichmentService: EnrichmentService) : MqttCallback {

    fun processReadingsTopic(message: MqttMessage?) =
        message.toString()
                .toResultOr { Exception("empty string") }
                .flatMap { Result.of {  Klaxon().parse<Reading>(it)!! } }
                .flatMap { enrichmentService.enrichReading(it) }
                .flatMap { influxDAO.writeReading(it) }
                .mapBoth(success = { println("wrote to database") }, failure = { println("There was an error! $it") })


    fun processEventsTopic(message: MqttMessage?) =
        message.toString()
                .toResultOr { Exception("empty string") }
                .flatMap { Klaxon().parse<Event>(it).toResultOr { Exception("parse error!!!") } }
                .flatMap { influxDAO.writeEvent(it) }
                .mapBoth(success = { println("wrote to database") }, failure = { println("There was an error! $it") })

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        when (topic) {
            READINGS_TOPIC -> processReadingsTopic(message)
            EVENTS_TOPIC -> processEventsTopic(message)
        }
    }

    override fun connectionLost(cause: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
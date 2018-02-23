package com.dionysus.ingestor

import com.beust.klaxon.Klaxon
import com.dionysus.common.domain.Event
import com.dionysus.common.domain.READINGS_TOPIC
import com.dionysus.common.domain.EVENTS_TOPIC
import com.dionysus.common.domain.Reading
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.mapBoth
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

class IngestionController(private val influxDAO: InfluxDAO) : MqttCallback {
    override fun messageArrived(topic: String?, message: MqttMessage?) {
        val messageBody = message?.toString() ?: return

        when (topic) {
            READINGS_TOPIC -> Result.of { Klaxon().parse<Reading>(messageBody) }
            EVENTS_TOPIC -> Result.of { Klaxon().parse<Event>(messageBody) }
            else -> Err(Exception("wrong topic?"))
        }.flatMap {
            when (it) {
                is Reading -> influxDAO.writeReading(it)
                is Event -> influxDAO.writeEvent(it)
                else -> Err("unsupported object")
            }
        }.mapBoth(success = { println("wrote to database") }, failure = { println("There was an error! $it") })
    }

    override fun connectionLost(cause: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
package com.dionysus.ingestor

import com.beust.klaxon.Klaxon
import com.dionysus.common.domain.Event
import com.dionysus.common.domain.READINGS_TOPIC
import com.dionysus.common.domain.RESULTS_TOPIC
import com.dionysus.common.domain.Reading
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.mapBoth
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

class IngestionController(val influxDAO: InfluxDAO) : MqttCallback {
    override fun messageArrived(topic: String?, message: MqttMessage?) {
        val messageBody = message?.payload.toString()

        when (topic) {
            READINGS_TOPIC -> Result.of { Klaxon().parse<Reading>(messageBody) }
            RESULTS_TOPIC -> Result.of { Klaxon().parse<Event>(messageBody) }
            else -> Err("can't parse")
        }.flatMap {
            when (it) {
                is Reading -> influxDAO.writeReading(it)
                is Event -> influxDAO.writeEvent(it)
                else -> Err("unsupported object")
            }
        }.mapBoth(success = { println("wrote to database") }, failure = { println("error! $it") })
    }

    override fun connectionLost(cause: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
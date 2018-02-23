package com.dionysus.ingestor

import com.dionysus.common.domain.READINGS_TOPIC
import com.dionysus.common.domain.EVENTS_TOPIC
import org.eclipse.paho.client.mqttv3.MqttClient

fun main(args: Array<String>) {

    val influxDAO = InfluxDAO("http://localhost:8086", "root", "root")
    val ingestionController = IngestionController(influxDAO)

    val mqttClient = MqttClient("tcp://localhost:1883", "ingestor").also { it.connect() }
    if (mqttClient.isConnected) {
        println("Connected to MQTT")
        mqttClient.subscribe(READINGS_TOPIC)
        mqttClient.subscribe(EVENTS_TOPIC)
        mqttClient.setCallback(ingestionController)
    } else {
        println("Connection to MQTT failed")
    }
}
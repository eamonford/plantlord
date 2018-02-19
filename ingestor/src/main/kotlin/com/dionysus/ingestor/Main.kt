package com.dionysus.ingestor

import org.eclipse.paho.client.mqttv3.MqttClient

fun main(args: Array<String>) {

    val influxDAO = InfluxDAO("http://localhost:8086", "username", "password")
    val ingestionController = IngestionController(influxDAO)

    val mqttClient = MqttClient("url", "ingestor")
    mqttClient.subscribe("dionysus/moisture")
    mqttClient.subscribe("dionysus/events")
    mqttClient.setCallback(ingestionController)
}
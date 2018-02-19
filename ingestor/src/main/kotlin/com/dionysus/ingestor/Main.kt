package com.dionysus.ingestor

import org.eclipse.paho.client.mqttv3.MqttClient

fun main(args: Array<String>) {

    val influxDAO = InfluxDAO("url", "username", "password")
    val ingestionController = IngestionController(influxDAO)

    val mqttClient = MqttClient("url", "ingestor")
    mqttClient.subscribe("dionysus/miosture")
    mqttClient.setCallback(ingestionController)
}
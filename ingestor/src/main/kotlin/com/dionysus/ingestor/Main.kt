package com.dionysus.ingestor

import org.eclipse.paho.client.mqttv3.MqttClient

fun main(args: Array<String>) {
    println("hello world")

    val mqttClient: MqttClient = MqttClient("url", "ingestor")
    mqttClient.subscribe("dionysus/miosture")
    mqttClient.setCallback()
}
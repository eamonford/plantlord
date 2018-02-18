package com.dionysus.irrigator
//import org.eclipse.paho.client.mqttv3.MqttClient
//
//class MQTTStream(val serverUri: String, val clientId: String) {
//    val mqttClient = MqttClient(serverUri, clientId)
//    init {
//        mqttClient.connect()
//    }
//
//    fun subscribe(topicFilter: String): MQTTStream {
//        mqttClient.subscribe(topicFilter)
//        return this
//    }
//
//    fun map(() -> )
//}
package com.dionysus.ingestor

import com.dionysus.common.domain.EVENTS_TOPIC
import com.dionysus.common.domain.READINGS_TOPIC
import com.dionysus.ingestor.dao.InfluxDAO
import com.dionysus.irrigator.dao.RuleRepository
import com.github.michaelbull.result.getOr
import org.eclipse.paho.client.mqttv3.MqttClient

fun main(args: Array<String>) {

    val ruleRepository = RuleRepository().connect().getOr { throw Exception("rule repository didn't work") }
//    ruleRepository.testTransaction()



    val enrichmentService = EnrichmentService(ruleRepository)
    val influxDAO = InfluxDAO("http://localhost:8086", "root", "root")
    val ingestionController = IngestionController(influxDAO, enrichmentService)

    val mqttClient = MqttClient("tcp://localhost:1883", "new_ingestor").also { it.connect() }
    if (mqttClient.isConnected) {
        println("Connected to MQTT")
        mqttClient.subscribe(READINGS_TOPIC)
        mqttClient.subscribe(EVENTS_TOPIC)
        mqttClient.setCallback(ingestionController)
    } else {
        println("Connection to MQTT failed")
    }
}
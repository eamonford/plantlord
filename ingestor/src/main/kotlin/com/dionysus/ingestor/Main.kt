package com.dionysus.ingestor

import com.dionysus.common.domain.EVENTS_TOPIC
import com.dionysus.common.domain.READINGS_TOPIC
import com.dionysus.common.domain.SensorsService
import com.dionysus.ingestor.dao.InfluxDAO
import com.dionysus.irrigator.dao.PostgresDAO
import org.eclipse.paho.client.mqttv3.MqttClient

fun main(args: Array<String>) {

    val postgresDAO = PostgresDAO.connect(
            EnvironmentConfig[postgres.url],
            EnvironmentConfig[postgres.username],
            EnvironmentConfig[postgres.password])

    val sensorsService = SensorsService(postgresDAO)
    val enrichmentService = EnrichmentService(sensorsService)
    val influxDAO = InfluxDAO.connect(
            EnvironmentConfig[influx.url],
            EnvironmentConfig[influx.username],
            EnvironmentConfig[influx.password])
    
    val ingestionController = IngestionController(influxDAO, enrichmentService)

    val mqttClient = MqttClient(EnvironmentConfig[mqtt.url], EnvironmentConfig[mqtt.clientid]).also { it.connect() }
    if (mqttClient.isConnected) {
        println("Connected to MQTT")
        mqttClient.subscribe(READINGS_TOPIC)
        mqttClient.subscribe(EVENTS_TOPIC)
        mqttClient.setCallback(ingestionController)
    } else {
        println("Connection to MQTT failed")
    }
}
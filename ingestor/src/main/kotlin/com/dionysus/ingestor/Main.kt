package com.dionysus.ingestor

import com.dionysus.common.domain.EVENTS_TOPIC
import com.dionysus.common.domain.READINGS_TOPIC
import com.dionysus.common.domain.SensorsService
import com.dionysus.ingestor.dao.InfluxDAO
import com.dionysus.irrigator.dao.DionysusConnectionException
import com.dionysus.irrigator.dao.PostgresDAO
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttClient

private val logger = KotlinLogging.logger {}

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

    val mqttUrl = EnvironmentConfig[mqtt.url]
   try {
       val mqttClient = MqttClient(mqttUrl, EnvironmentConfig[mqtt.clientid]).also { it.connect() }
       mqttClient.subscribe(READINGS_TOPIC)
       mqttClient.subscribe(EVENTS_TOPIC)
       logger.info { "Connected to MQTT at $mqttUrl" }

       mqttClient.setCallback(ingestionController)
   } catch (e: Throwable) {
       throw DionysusConnectionException("Connection to MQTT failed for $mqttUrl", e)
   }
}
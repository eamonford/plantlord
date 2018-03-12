package com.dionysus.ingestor

import com.dionysus.common.EVENTS_TOPIC
import com.dionysus.common.READINGS_TOPIC
import com.dionysus.common.dao.PostgresDAO
import com.dionysus.common.exceptions.DionysusConnectionException
import com.dionysus.common.services.SensorsService
import com.dionysus.ingestor.config.EnvironmentConfig
import com.dionysus.ingestor.config.influx
import com.dionysus.ingestor.config.mqtt
import com.dionysus.ingestor.config.postgres
import com.dionysus.ingestor.dao.InfluxDAO
import com.dionysus.ingestor.services.EnrichmentService
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttClient
import org.flywaydb.core.Flyway


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

    val flyway = Flyway()
    flyway.setDataSource(
            EnvironmentConfig[postgres.url],
            EnvironmentConfig[postgres.username],
            EnvironmentConfig[postgres.password])
    flyway.migrate()

    val mqttUrl = EnvironmentConfig[mqtt.url]
    try {
        val mqttClient = MqttClient(mqttUrl, EnvironmentConfig[mqtt.clientid]).also { it.connect() }
        mqttClient.subscribe(READINGS_TOPIC)
        mqttClient.subscribe(EVENTS_TOPIC)
        logger.info { "Connected to MQTT at $mqttUrl" }

        logger.info { "Ingestor has started." }
        mqttClient.setCallback(ingestionController)
    } catch (e: Throwable) {
        throw DionysusConnectionException("Connection to MQTT failed for $mqttUrl", e)
    }
}
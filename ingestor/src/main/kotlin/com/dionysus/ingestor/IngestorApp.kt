package com.dionysus.ingestor

import com.dionysus.common.dao.InfluxDAO
import com.dionysus.common.dao.PostgresDAO
import com.dionysus.common.services.EventsService
import com.dionysus.common.services.ReadingsService
import com.dionysus.common.services.SensorsService
import com.dionysus.common.services.ValvesService
import com.dionysus.ingestor.config.IngestorConfig
import com.dionysus.ingestor.config.influx
import com.dionysus.ingestor.config.mqtt
import com.dionysus.ingestor.config.postgres
import com.dionysus.ingestor.services.EnrichmentService
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.flywaydb.core.Flyway

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {

    val postgresDAO = PostgresDAO.connect(
            IngestorConfig[postgres.url],
            IngestorConfig[postgres.username],
            IngestorConfig[postgres.password])
    val sensorsService = SensorsService(postgresDAO)
    val valvesService = ValvesService(postgresDAO)
    val enrichmentService = EnrichmentService(sensorsService, valvesService)
    val influxDAO = InfluxDAO.connect(
            IngestorConfig[influx.url],
            IngestorConfig[influx.username],
            IngestorConfig[influx.password])
    val readingsService = ReadingsService(influxDAO)
    val eventsService = EventsService(influxDAO)
    val flyway = Flyway()
    flyway.setDataSource(
            IngestorConfig[postgres.url],
            IngestorConfig[postgres.username],
            IngestorConfig[postgres.password])
    flyway.migrate()

    val mqttClient = MqttAsyncClient(IngestorConfig[mqtt.url], IngestorConfig[mqtt.clientid])
    IngestionController(
            readingsService = readingsService,
            eventsService = eventsService,
            enrichmentService = enrichmentService,
            mqttClient = mqttClient)
    logger.info { "Ingestor has started." }
}
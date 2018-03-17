package com.dionysus.ingestor

import com.dionysus.common.dao.PostgresDAO
import com.dionysus.common.services.SensorsService
import com.dionysus.ingestor.config.IngestorConfig
import com.dionysus.ingestor.config.influx
import com.dionysus.ingestor.config.mqtt
import com.dionysus.ingestor.config.postgres
import com.dionysus.ingestor.dao.InfluxDAO
import com.dionysus.ingestor.services.EnrichmentService
import mu.KotlinLogging
import org.flywaydb.core.Flyway

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {

    val postgresDAO = PostgresDAO.connect(
            IngestorConfig[postgres.url],
            IngestorConfig[postgres.username],
            IngestorConfig[postgres.password])
    val sensorsService = SensorsService(postgresDAO)
    val enrichmentService = EnrichmentService(sensorsService)
    val influxDAO = InfluxDAO.connect(
            IngestorConfig[influx.url],
            IngestorConfig[influx.username],
            IngestorConfig[influx.password])


    val flyway = Flyway()
    flyway.setDataSource(
            IngestorConfig[postgres.url],
            IngestorConfig[postgres.username],
            IngestorConfig[postgres.password])
    flyway.migrate()

    IngestionController(influxDAO, enrichmentService, IngestorConfig[mqtt.url], IngestorConfig[mqtt.clientid])
    logger.info { "Ingestor has started." }

}
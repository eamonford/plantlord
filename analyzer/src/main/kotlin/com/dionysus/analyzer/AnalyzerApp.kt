package com.dionysus.analyzer

import com.dionysus.analyzer.services.AnalyzerService
import com.dionysus.common.dao.InfluxDAO
import com.dionysus.common.services.EventsService
import com.dionysus.common.services.ReadingsService
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {

    val influxDAO = InfluxDAO.connect(
            AnalyzerConfig[influx.url],
            AnalyzerConfig[influx.username],
            AnalyzerConfig[influx.password])

    val readingsService = ReadingsService(influxDAO)
    val eventsService = EventsService(influxDAO)
    val analyzerService = AnalyzerService(
            readingsService = readingsService,
            eventsService = eventsService,
            period = AnalyzerConfig[analysis.period],
            delay = AnalyzerConfig[analysis.delay],
            windowBefore = AnalyzerConfig[analysis.windowBefore],
            windowAfter = AnalyzerConfig[analysis.windowAfter])

    logger.info { "Analyzer has started." }
    analyzerService.scheduleAnalysis()
}
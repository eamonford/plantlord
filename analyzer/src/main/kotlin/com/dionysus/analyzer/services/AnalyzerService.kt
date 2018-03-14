package com.dionysus.analyzer.services

import com.dionysus.common.domain.EnrichedEvent
import com.dionysus.common.domain.EnrichedReading
import com.dionysus.common.exceptions.DionysusException
import com.dionysus.common.exceptions.DionysusNotFoundException
import com.dionysus.common.services.EventsService
import com.dionysus.common.services.ReadingsService
import com.github.michaelbull.result.*
import mu.KotlinLogging
import java.time.Instant
import kotlin.concurrent.timer

private val logger = KotlinLogging.logger {}

class AnalyzerService(private val readingsService: ReadingsService,
                      private val eventsService: EventsService,
                      period: Long,
                      delay: Long,
                      windowBefore: Long,
                      windowAfter: Long) {

    private val periodInMillis = period * 1000L
    private val delayInMillis = delay * 1000L
    private val windowBeforeInMillis = windowBefore * 1000L
    private val windowAfterInMillis = windowAfter * 1000L

    private data class Analysis(val event: EnrichedEvent,
                                val readingBeforeEvent: EnrichedReading? = null,
                                val readingsAfterEvent: List<EnrichedReading>? = null,
                                val efficacyRatio: Double? = null)

    fun scheduleAnalysis() = timer(name = "analyzer", period = periodInMillis, action = { runAnalysis() })

    private fun runAnalysis() =
            findEventsInPeriodWindow().map {
                logger.info { "Found ${it.size} irrigation events in the search window." }
                it.forEach { analyzeEvent(it) }
            }

    private fun findEventsInPeriodWindow(): Result<List<EnrichedEvent>, Throwable> {
        val now = Instant.now()
        val eventSearchWindowEnd = now.toEpochMilli() - windowAfterInMillis - delayInMillis
        val eventSearchWindowStart = eventSearchWindowEnd - periodInMillis
        val eventSearchWindow = LongRange(eventSearchWindowStart, eventSearchWindowEnd)

        logger.info { "Searching for irrigation events in window ${Instant.ofEpochMilli(eventSearchWindowStart)} - ${Instant.ofEpochMilli(eventSearchWindowEnd)}..." }
        return eventsService.findEvents(dateRange = eventSearchWindow)
                .map {
                    it
                            .groupBy { it.plantId }
                            .values
                            .map { it.last() }
                }
    }

    private fun getLastReadingBeforeEvent(analysis: Analysis): Result<Analysis, Throwable> {
        val event = analysis.event
        val startOfEvent = event.timestamp - event.value
        val windowForReadingsBeforeEvent = LongRange(startOfEvent - windowBeforeInMillis, startOfEvent)
        return readingsService
                .findReadings(plantId = event.plantId, dateRange = windowForReadingsBeforeEvent)
                .flatMap {
                    if (it.isEmpty()) Err(DionysusNotFoundException("No acceptable readings found before this event"))
                    else Ok(it.last())
                }
                .map { analysis.copy(readingBeforeEvent = it) }
    }

    private fun getAcceptableReadingsAfterEvent(analysis: Analysis): Result<Analysis, Throwable> {
        val event = analysis.event
        val startOfAcceptableWindow = event.timestamp + delayInMillis
        val windowForReadingsAfterEvent = LongRange(startOfAcceptableWindow, startOfAcceptableWindow + windowAfterInMillis)
        return readingsService
                .findReadings(plantId = event.plantId, dateRange = windowForReadingsAfterEvent)
                .flatMap {
                    if (it.isEmpty()) Err(DionysusNotFoundException("No acceptable readings were found after this event"))
                    else Ok(it)
                }
                .map { analysis.copy(readingsAfterEvent = it) }
    }

    private fun filterReadingsByHighLow(analysis: Analysis): Result<Analysis, Throwable> =
            Result.of {
                analysis.copy(readingsAfterEvent = analysis.readingsAfterEvent
                        ?.filter { it.value < 100 }
                        ?.filter { it.value > 0 }
                        ?: analysis.readingsAfterEvent)
            }.flatMap {
                when (it.readingsAfterEvent?.isNotEmpty()) {
                    true -> Ok(it)
                    else -> Err(DionysusException("There are no post-event readings left after filtering by high/low. Before filtering, there were ${analysis.readingsAfterEvent?.size}."))
                }
            }

    private fun filterReadingsByRelativeDecrease(analysis: Analysis): Result<Analysis, Throwable> =
            Result.of {
                analysis.copy(readingsAfterEvent =
                analysis.readingsAfterEvent
                        ?.filter { it.value > analysis.readingBeforeEvent?.value ?: it.value }
                        ?: analysis.readingsAfterEvent)
            }.flatMap {
                when (it.readingsAfterEvent?.isNotEmpty()) {
                    true -> Ok(it)
                    else -> Err(DionysusException("There are no post-event readings left after filtering by relative decrease. Before filtering, there were ${analysis.readingsAfterEvent?.size}."))
                }
            }

    private fun calculateEfficacyRatio(analysis: Analysis): Analysis =
            when {
                (analysis.readingBeforeEvent == null || analysis.readingsAfterEvent == null) -> analysis
                else -> {
                    val averageAfterEvent = analysis.readingsAfterEvent.map { it.value }.average()
                    val moistureDelta = averageAfterEvent - analysis.readingBeforeEvent.value.toDouble()
                    analysis.copy(efficacyRatio = moistureDelta / analysis.event.value.toDouble())
                }
            }

    private fun updateEventWithEfficacyRatio(analysis: Analysis): Result<Analysis, Throwable> =
            eventsService
                    .write(analysis.event.copy(efficacy = analysis.efficacyRatio))
                    .map { analysis.copy(event = it) }

    private fun analyzeEvent(event: EnrichedEvent) =
            Ok(Analysis(event = event))
                    .flatMap { getLastReadingBeforeEvent(it) }
                    .flatMap { getAcceptableReadingsAfterEvent(it) }
                    .flatMap { filterReadingsByRelativeDecrease(it) }
                    .flatMap { filterReadingsByHighLow(it) }
                    .map { calculateEfficacyRatio(it) }
                    .map { logger.info { "The efficacy ratio of this event is ${it.efficacyRatio}." }; it }
                    .flatMap { updateEventWithEfficacyRatio(it) }
                    .mapEither(
                            success = { logger.info { "Updated event in database with efficacy ratio ${it.efficacyRatio}." }; it },
                            failure = { logger.error("There was an error analyzing this event", it); it })
}
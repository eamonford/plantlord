package com.dionysus.common.services

import com.dionysus.common.dao.InfluxDAO
import com.dionysus.common.domain.EnrichedEvent
import com.dionysus.common.domain.Event
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import org.influxdb.dto.Point
import java.time.Instant
import java.util.concurrent.TimeUnit

open class EventsService(private val influxDAO: InfluxDAO) {

    fun findEvents(plantId: Int? = null, dateRange: LongRange? = null): Result<List<EnrichedEvent>, Throwable> =
            influxDAO.find(
                    measurement = "events",
                    dateRange = dateRange,
                    tagMatchers = if (plantId != null) hashMapOf(Pair("plantId", plantId.toString())) else null)
                    .flatMap {
                        Result.of { it.map { EnrichedEvent.fromInfluxQueryResult(it) } }
                    }

    open fun write(event: EnrichedEvent): Result<EnrichedEvent, Throwable> = Result.of { influxDAO.write(event.toPoint()); event }
}

fun EnrichedEvent.Companion.fromInfluxQueryResult(queryResult: Map<String, Any?>): EnrichedEvent =
        EnrichedEvent(
                baseEvent = Event(
                        valveId = (queryResult["valveId"] as String).toInt(),
                        value = (queryResult["value"] as Double).toInt()),
                plantId = (queryResult["plantId"] as String?)?.toInt() ?: 0,
                timestamp = Instant.parse(queryResult["time"] as String).toEpochMilli(),
                efficacy = (queryResult["efficacy"] as Double?))

fun EnrichedEvent.toPoint(): Point =
        Point
                .measurement("events")
                .time(timestamp, TimeUnit.MILLISECONDS)
                .addField("value", value)
                .addField("efficacy", efficacy)
                .tag("valveId", valveId.toString())
                .tag("plantId", plantId.toString())
                .build()

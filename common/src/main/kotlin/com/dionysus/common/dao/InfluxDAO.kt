package com.dionysus.common.dao

import com.dionysus.common.exceptions.DionysusConnectionException
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import mu.KotlinLogging
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.influxdb.dto.Query

private val logger = KotlinLogging.logger {}

open class InfluxDAO private constructor(private val influxDB: InfluxDB) {

    companion object {
        fun connect(url: String, username: String, password: String): InfluxDAO {
            val influxDB: InfluxDB = InfluxDBFactory.connect(url, username, password).setDatabase("dionysus")
            val dao = InfluxDAO(influxDB).testConnection(url)
            logger.info { "Connected to InfluxDB at $url" }
            return dao
        }
    }

    fun testConnection(url: String): InfluxDAO {
        try {
            influxDB.ping().version.startsWith("v", ignoreCase = true)
        } catch (e: Throwable) {
            throw DionysusConnectionException("Could not connect to InfluxDB at $url", e)
        }
        return this
    }

    fun find(measurement: String, dateRange: LongRange? = null, tagMatchers: Map<String, Any>? = null): Result<List<Map<String, Any?>>, Throwable> =
            Result.of {
                val queryStringBuilder = StringBuilder("select * from $measurement")
                if (dateRange != null)
                    queryStringBuilder.append(" where time > ${dateRange.start * 1000000} and time <= ${dateRange.endInclusive * 1000000}")
                if (tagMatchers?.isNotEmpty() == true) {
                    queryStringBuilder.append(if (dateRange == null) " where " else " and ")
                    val matchString = tagMatchers
                            .entries
                            .joinToString(separator = " and ", transform = { "${it.key} = '${it.value}'" })
                    queryStringBuilder.append(matchString)
                }

                val query = Query(queryStringBuilder.toString(), "dionysus")
                val result = influxDB.query(query).results.first()
                if (result.series == null)
                    return Ok(ArrayList())
                val series = result.series.first()
                series
                        .values
                        .map { series.columns.zip(it).toMap() }
            }

    fun write(point: Point) = influxDB.write(point)
}
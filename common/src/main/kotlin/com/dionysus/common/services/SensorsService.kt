package com.dionysus.common.services

import com.dionysus.common.dao.PostgresDAO
import com.dionysus.common.domain.Sensor
import com.dionysus.common.exceptions.DionysusNotFoundException
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction


object Sensors : IntIdTable() {
    val deviceId = text("device_id")
    val name = text("name")
    val plantId = integer("plant_id")
}

class SensorDTO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SensorDTO>(Sensors)

    var deviceId by Sensors.deviceId
    var name by Sensors.name
    var plantId by Sensors.plantId

    fun toSensor() = Sensor(deviceId = deviceId, name = name, type = "moisture", plantId = plantId)
}

class SensorsService(DAO: PostgresDAO) {
    fun getSensorByDeviceId(deviceId: String): Result<Sensor, Throwable> =
            Result.of {
                transaction {
                    SensorDTO
                            .find { Sensors.deviceId eq deviceId }
                            .first()
                }
            }.mapError {
                when (it) {
                    is NoSuchElementException -> DionysusNotFoundException("There is no sensor with device ID $deviceId", it)
                    else -> it
                }
            }.map { it.toSensor() }
}
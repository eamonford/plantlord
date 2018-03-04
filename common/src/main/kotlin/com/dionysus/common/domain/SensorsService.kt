package com.dionysus.common.domain

import com.dionysus.irrigator.dao.PostgresDAO
import com.github.michaelbull.result.Result
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
    val rules by RuleDTO referrersOn Rules.sensor

    fun toSensor() = Sensor(deviceId = deviceId, name = name, type = "moisture")
}

class SensorsService(DAO: PostgresDAO) {
    fun getSensorByDeviceId(deviceId: String): Result<Sensor, Throwable> =
            Result.of {
                transaction {
                    SensorDTO
                            .find { Sensors.deviceId eq deviceId }
                            .first()
                }.toSensor()
            }
}

package com.dionysus.irrigator.dao

import com.dionysus.common.domain.Rule
import com.dionysus.common.domain.Sensor
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

open class RuleRepository {

    fun connect(): Result<RuleRepository, Throwable> {
        println("Trying to connect to Postgres")
        return Result
                .of {
                    Database.connect(
                            url = "jdbc:postgresql://localhost/database",
                            driver = "org.postgresql.Driver",
                            user = "admin",
                            password = "password"
                    )
                }
                .map { println("Connected to Postgres."); this }
    }

    fun getRuleById(id: Int): Rule = transaction { RuleDTO[id].toRule() }

//    fun createRule(sensorId: String, type: String, threshold: Int): Result<Rule, Throwable> =
//            Result.of {
//                transaction {
//                    RuleDTO.new {
//                        this.sensorId = sensorId
//                        this.type = type
//                        this.threshold = threshold
//                    }
//                }.toRule()
//            }

    fun getRuleBySensorDeviceId(deviceId: String) = {

        Rules.select { Rules.id eq 1 }
        Result.of {
            transaction {
                (Rules innerJoin Sensors)
                        .select { Sensors.deviceId eq deviceId }
                        .first()

            }
        }
    }

    fun testTransaction() {
        // TRY THIS
        transaction {
            Rules.insert {
                it[type] = "test"
                it[threshold] = 10
                it[target] = 90
                it[valveId] = 100
                it[moistureToLitersRatio] = BigDecimal(10.0)
            }
        }
    }

    fun getSensorByDeviceId(deviceId: String): Result<Sensor, Throwable> =
            Result.of {
                transaction {
                    SensorDTO
                            .find { Sensors.deviceId eq deviceId }
                            .first()
                }.toSensor()
            }


//    fun getBySensorId(sensorId: String): Result<Rule, Throwable> {
//        val result = transaction {
//            (Rules innerJoin Sensors).slice(Rules.sensor, Sensors.deviceId).selectAll()
//        }
//
//        return Result.of {
//            transaction {
//                RuleDTO
//                        .find { Rules.sensor eq sensorId }
//                        .limit(1)
//                        .first()
//            }.toRule()
//        }
//    }
}

object Rules : IntIdTable() {
    val sensor = reference("sensor_id", Sensors)
    val type = text("type")
    val threshold = integer("threshold")
    val target = integer("target")
    val valveId = integer("valve_id")
    val moistureToLitersRatio = decimal("moisture_to_liters", 10, 3)
}

class RuleDTO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RuleDTO>(Rules)

    var sensor by SensorDTO referencedOn Rules.sensor
    var type by Rules.type
    var threshold by Rules.threshold
    var target by Rules.target
    var valveId by Rules.valveId
    var moistureToLitersRatio by Rules.moistureToLitersRatio

    fun toRule() = Rule(
            sensorName = sensor.name,
            type = type,
            threshold = threshold,
            valveId = valveId,
            moistureToLitersRatio = moistureToLitersRatio.toDouble(),
            target = target)
}

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
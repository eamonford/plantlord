package com.dionysus.common.services

import com.dionysus.common.dao.PostgresDAO
import com.dionysus.common.domain.Rule
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction


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

class RulesService(DAO: PostgresDAO) {
    fun getRuleById(id: Int): Rule = transaction { RuleDTO[id].toRule() }
}
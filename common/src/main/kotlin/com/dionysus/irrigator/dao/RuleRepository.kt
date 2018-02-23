package com.dionysus.irrigator.dao
import com.dionysus.common.domain.Rule
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

open class RuleRepository {

    fun connect(): Result<RuleRepository, Throwable> {
        println("Trying to connect to Postgres")
        return Result
                .of { Database.connect(url ="jdbc:postgresql://localhost/database", driver = "org.postgresql.Driver", user = "admin", password = "password") }
                .map { println("Connected to Postgres."); this }
    }

    fun getRuleById(id: Int): Rule = transaction { RuleDTO[id].toRule() }

    fun createRule(sensorId: String, type: String, threshold: Int): Result<Rule, Throwable> =
            Result.of {
                transaction {
                    RuleDTO.new {
                        this.sensorId = sensorId
                        this.type = type
                        this.threshold = threshold
                    }
                }.toRule()
            }

    fun getBySensorId(sensorId: String): Result<Rule, Throwable> =
            Result.of {
                transaction {
                    RuleDTO
                            .find { Rules.sensorId eq sensorId }
                            .limit(1)
                            .first()
                }.toRule()
            }
}

object Rules: IntIdTable() {
    val sensorId = text("sensor_id")
    val type = text("type")
    val threshold = integer("threshold")
    val target = integer("target")
    val valveId = integer("valve_id")
    val moistureToLitersRatio = decimal("moisture_to_liters", 10, 3)
}

private class RuleDTO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RuleDTO>(Rules)
    var sensorId by Rules.sensorId
    var type by Rules.type
    var threshold by Rules.threshold
    var target by Rules.target
    var valveId by Rules.valveId
    var moistureToLitersRatio by Rules.moistureToLitersRatio

    fun toRule() = Rule(
            sensorId = sensorId,
            type = type,
            threshold = threshold,
            valveId = valveId,
            moistureToLitersRatio = moistureToLitersRatio.toDouble(),
            target = target)
}
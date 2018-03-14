package com.dionysus.common.services

import com.dionysus.common.dao.PostgresDAO
import com.dionysus.common.domain.Valve
import com.dionysus.common.exceptions.DionysusNotFoundException
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Valves : IntIdTable() {
    val hardwareId = integer("hardware_id")
    val plantId = integer("plant_id")
}

class ValveDto(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ValveDto>(Valves)

    var hardwareId by Valves.hardwareId
    var plantId by Valves.plantId

    fun toValve() = Valve(hardwareId = hardwareId, plantId = plantId)
}

class ValvesService(DAO: PostgresDAO) {
    fun getByHardwareId(hardwareId: Int): Result<Valve, Throwable> =
            Result.of {
                transaction {
                    ValveDto
                            .find { Valves.hardwareId eq hardwareId }
                            .first()
                }
            }.mapError {
                when (it) {
                    is NoSuchElementException -> DionysusNotFoundException("There is no valve with hardware ID $hardwareId", it)
                    else -> it
                }
            }.map { it.toValve() }
}

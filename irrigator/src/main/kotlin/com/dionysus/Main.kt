package com.dionysus

import com.dionysus.dao.RuleRepository
import com.github.michaelbull.result.getOr

fun main(args: Array<String>) {
    val ruleRepository = RuleRepository().connect().getOr { throw Exception("rule repository didn't work") }
    IrrigationController(
            ruleRepository = ruleRepository,
            mqttUrl = "tcp://localhost:1883",
            clientId = "irrigator",
            sourceTopic = "irrigator.source",
            sinkTopic = "irrigator.sink")
}
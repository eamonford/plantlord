//package com.dionysus.irrigator
//
//import com.dionysus.common.dao.PostgresDAO
//
//fun main(args: Array<String>) {
//    val ruleRepository = PostgresDAO().connect().getOr { throw Exception("rule repository didn't work") }
//    IrrigationController(
//            postgresDAO = ruleRepository,
//            mqttUrl = "tcp://localhost:1883",
//            clientId = "irrigator",
//            sourceTopic = "irrigator.source",
//            sinkTopic = "irrigator.sink")
//}
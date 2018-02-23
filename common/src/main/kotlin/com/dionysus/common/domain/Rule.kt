package com.dionysus.common.domain

data class Rule (val sensorId: String,
                 val type: String = "",
                 val threshold: Int,
                 val target: Int,
                 val valveId: Int,
                 val moistureToLitersRatio: Double)
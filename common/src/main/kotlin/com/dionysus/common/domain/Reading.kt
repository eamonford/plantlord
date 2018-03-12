package com.dionysus.common.domain

data class Reading(
        val deviceId: String,
        val battery: Double,
        val value: Int) : Message
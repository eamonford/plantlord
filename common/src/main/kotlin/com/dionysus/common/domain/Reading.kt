package com.dionysus.common.domain

data class Reading(val deviceId: String, val battery: Int, val value: Double) : Message
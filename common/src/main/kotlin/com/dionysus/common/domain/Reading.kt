package com.dionysus.common.domain

import com.beust.klaxon.Json

data class Reading(
        @Json("device_id") val deviceId: String,
        val battery: Double,
        val value: Int) : Message
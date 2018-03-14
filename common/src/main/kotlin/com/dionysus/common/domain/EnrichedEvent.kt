package com.dionysus.common.domain

data class EnrichedEvent(private val baseEvent: Event,
                         val plantId: Int,
                         val efficacy: Double? = null,
                         val timestamp: Long = System.currentTimeMillis()) {
    companion object
    val valveId: Int by lazy { baseEvent.valveId }
    val value: Int by lazy { baseEvent.value }
}
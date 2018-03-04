package com.dionysus.irrigator.dao

class DionysusConnectionException(override val message: String?, override val cause: Throwable?) : RuntimeException(message, cause)
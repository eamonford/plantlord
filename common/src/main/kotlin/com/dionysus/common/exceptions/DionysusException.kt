package com.dionysus.common.exceptions

open class DionysusException(override val message: String?, override val cause: Throwable? = null) : RuntimeException(message, cause)
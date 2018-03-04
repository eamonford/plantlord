package com.dionysus.common.exceptions

class DionysusConnectionException(override val message: String?, override val cause: Throwable? = null) : DionysusException(message, cause)
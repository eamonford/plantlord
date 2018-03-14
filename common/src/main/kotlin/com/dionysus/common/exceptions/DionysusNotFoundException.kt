package com.dionysus.common.exceptions

class DionysusNotFoundException(override val message: String?, override val cause: Throwable? = null) : DionysusException(message, cause)
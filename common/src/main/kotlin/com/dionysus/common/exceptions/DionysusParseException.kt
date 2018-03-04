package com.dionysus.common.exceptions

class DionysusParseException(override val message: String?, override val cause: Throwable? = null) : DionysusException(message, cause)
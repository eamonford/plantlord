package com.dionysus.ingestor.config

import com.natpryce.konfig.*

object influx : PropertyGroup() {
    val url by stringType
    val username by stringType
    val password by stringType
}

object mqtt : PropertyGroup() {
    val url by stringType
    val clientid by stringType
}

object postgres : PropertyGroup() {
    val url by stringType
    val username by stringType
    val password by stringType
}

val IngestorConfig = EnvironmentVariables() overriding
ConfigurationProperties.fromResource("defaults.properties")

package com.dionysus.analyzer

import com.natpryce.konfig.*

object influx : PropertyGroup() {
    val url by stringType
    val username by stringType
    val password by stringType
}

object analysis : PropertyGroup() {
    val period by longType
    val delay by longType
    val windowBefore by longType
    val windowAfter by longType
}

val AnalyzerConfig = EnvironmentVariables() overriding
        ConfigurationProperties.fromResource("defaults.properties")

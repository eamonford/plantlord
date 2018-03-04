package com.dionysus.irrigator.dao

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database

private val logger = KotlinLogging.logger {}

open class PostgresDAO private constructor(private val database: Database) {
    companion object {

        fun connect(url: String, username: String, password: String): PostgresDAO {
            val database = Database.connect(
                    url = url,
                    driver = "org.postgresql.Driver",
                    user = username,
                    password = password)
            val dao = PostgresDAO(database).testConnection(url)
            logger.info { "Connected to Postgres at $url" }
            return dao
        }
    }

    fun testConnection(url: String): PostgresDAO {
        try {
            database.version
        } catch (e: Exception) {
            throw DionysusConnectionException("Could not connect to Postgres at $url", e)
        }
        return this
    }
}
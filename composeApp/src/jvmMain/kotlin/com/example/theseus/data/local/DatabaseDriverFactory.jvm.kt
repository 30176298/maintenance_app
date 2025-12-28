package com.example.theseus.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.theseus.database.TheseusDatabase
import java.io.File
import java.util.Properties

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".theseus")
        databasePath.mkdirs()
        val databaseFile = File(databasePath, "theseus.db")

        val databaseExists = databaseFile.exists()

        val driver = JdbcSqliteDriver(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
            properties = Properties()
        )

        // Only create schema if database doesn't exist
        if (!databaseExists) {
            TheseusDatabase.Schema.create(driver)
        }

        return driver
    }
}
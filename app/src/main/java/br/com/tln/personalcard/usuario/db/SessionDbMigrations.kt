package br.com.tln.personalcard.usuario.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SessionDbMigrations {
    val MIGRATIONS by lazy {
        arrayOf(MIGRATION_1_2)
    }

    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {

        }
    }
}
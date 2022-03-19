package com.ssmmhh.jibam.persistence

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ssmmhh.jibam.persistence.entities.CategoryEntity
import com.ssmmhh.jibam.persistence.entities.CategoryImageEntity
import com.ssmmhh.jibam.persistence.entities.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        CategoryImageEntity::class
    ],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 4, to = 5)]
    ,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getCategoriesDao(): CategoriesDao

    abstract fun getRecordsDao(): RecordsDao

    companion object {
        val DATABASE_NAME: String = "app_db"
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new records table
                database.execSQL("CREATE TABLE IF NOT EXISTS `Records_new` (" +
                        "`rId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`money` REAL NOT NULL, " +
                        "`memo` TEXT, " +
                        "`cat_id` INTEGER NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`cat_id`) REFERENCES `categories`(`cId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                // Copy the data from old records
                database.execSQL("INSERT INTO `Records_new` (rId, money, memo, cat_id, date) " +
                        "SELECT rId, money, memo, cat_id, date " +
                        "FROM records")
                // Remove old table
                database.execSQL("DROP TABLE records")
                // Change name of table to correct one
                database.execSQL("ALTER TABLE Records_new RENAME TO records")
            }
        }

    }
}
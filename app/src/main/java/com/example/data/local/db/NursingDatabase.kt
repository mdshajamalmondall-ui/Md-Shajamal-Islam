package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ChatThreadEntity::class,
        ChatMessageEntity::class,
        SearchHistoryEntity::class,
        SavedAnswerEntity::class,
        KnowledgeDocEntity::class,
        DrugEntity::class,
        ProcedureEntity::class,
        StudyItemEntity::class,
        McqQuestionEntity::class,
        AuditLogEntity::class,
        PersonalNoteEntity::class,
        UserFileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NursingDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun searchAndSavedDao(): SearchAndSavedDao
    abstract fun knowledgeDocDao(): KnowledgeDocDao
    abstract fun drugDao(): DrugDao
    abstract fun procedureDao(): ProcedureDao
    abstract fun studyItemDao(): StudyItemDao
    abstract fun mcqDao(): McqDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun personalNoteDao(): PersonalNoteDao
    abstract fun userFileDao(): UserFileDao

    companion object {
        @Volatile
        private var INSTANCE: NursingDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS personal_notes (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        tag TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_files (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        folder TEXT NOT NULL,
                        uriString TEXT NOT NULL,
                        mimeType TEXT NOT NULL,
                        sizeBytes INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        addedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): NursingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NursingDatabase::class.java,
                    "nursing_ai_clinical.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

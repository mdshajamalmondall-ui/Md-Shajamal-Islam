package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
        AuditLogEntity::class
    ],
    version = 1,
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

    companion object {
        @Volatile
        private var INSTANCE: NursingDatabase? = null

        fun getInstance(context: Context): NursingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NursingDatabase::class.java,
                    "nursing_ai_clinical.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

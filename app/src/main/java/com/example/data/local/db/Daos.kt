package com.example.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY isPinned DESC, updatedAt DESC")
    fun observeThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY createdAt ASC")
    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY createdAt ASC")
    suspend fun getMessagesOnce(threadId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThread(thread: ChatThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: String)

    @Query("UPDATE chat_threads SET updatedAt = :updatedAt, title = :title WHERE id = :threadId")
    suspend fun updateThreadMeta(threadId: String, title: String, updatedAt: Long)
}

@Dao
interface SearchAndSavedDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun observeSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Query("SELECT * FROM saved_answers ORDER BY savedAt DESC")
    fun observeSavedAnswers(): Flow<List<SavedAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAnswer(answer: SavedAnswerEntity)

    @Query("DELETE FROM saved_answers WHERE id = :id")
    suspend fun deleteSavedAnswer(id: String)
}

@Dao
interface KnowledgeDocDao {
    @Query("SELECT * FROM knowledge_docs ORDER BY subject ASC, titleEn ASC")
    fun observeAll(): Flow<List<KnowledgeDocEntity>>

    @Query("SELECT * FROM knowledge_docs")
    suspend fun getAllOnce(): List<KnowledgeDocEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(docs: List<KnowledgeDocEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(doc: KnowledgeDocEntity)

    @Query("DELETE FROM knowledge_docs WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface DrugDao {
    @Query("SELECT * FROM drugs ORDER BY genericName ASC")
    fun observeAll(): Flow<List<DrugEntity>>

    @Query("SELECT * FROM drugs")
    suspend fun getAllOnce(): List<DrugEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drugs: List<DrugEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(drug: DrugEntity)

    @Query("UPDATE drugs SET isOfflineSaved = :saved WHERE id = :id")
    suspend fun setOfflineSaved(id: String, saved: Boolean)
}

@Dao
interface ProcedureDao {
    @Query("SELECT * FROM procedures ORDER BY titleEn ASC")
    fun observeAll(): Flow<List<ProcedureEntity>>

    @Query("SELECT * FROM procedures")
    suspend fun getAllOnce(): List<ProcedureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(procedures: List<ProcedureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(procedure: ProcedureEntity)

    @Query("UPDATE procedures SET isOfflineSaved = :saved WHERE id = :id")
    suspend fun setOfflineSaved(id: String, saved: Boolean)
}

@Dao
interface StudyItemDao {
    @Query("SELECT * FROM study_items ORDER BY subject ASC, titleEn ASC")
    fun observeAll(): Flow<List<StudyItemEntity>>

    @Query("SELECT * FROM study_items")
    suspend fun getAllOnce(): List<StudyItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StudyItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: StudyItemEntity)

    @Query("UPDATE study_items SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmarked(id: String, bookmarked: Boolean)

    @Query("UPDATE study_items SET isOfflineSaved = :saved WHERE id = :id")
    suspend fun setOfflineSaved(id: String, saved: Boolean)
}

@Dao
interface McqDao {
    @Query("SELECT * FROM mcq_questions ORDER BY subject ASC")
    fun observeAll(): Flow<List<McqQuestionEntity>>

    @Query("SELECT * FROM mcq_questions")
    suspend fun getAllOnce(): List<McqQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<McqQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(question: McqQuestionEntity)

    @Update
    suspend fun update(question: McqQuestionEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun observeAll(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity)
}

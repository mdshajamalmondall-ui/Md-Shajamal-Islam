package com.example.data.local

import com.example.data.local.db.AuditLogEntity
import com.example.data.local.db.ChatMessageEntity
import com.example.data.local.db.ChatThreadEntity
import com.example.data.local.db.DrugAndProcedureSeedData
import com.example.data.local.db.DrugEntity
import com.example.data.local.db.KnowledgeDocEntity
import com.example.data.local.db.KnowledgeSeedData
import com.example.data.local.db.McqQuestionEntity
import com.example.data.local.db.NursingDatabase
import com.example.data.local.db.ProcedureEntity
import com.example.data.local.db.SavedAnswerEntity
import com.example.data.local.db.SearchHistoryEntity
import com.example.data.local.db.StudyAndMcqSeedData
import com.example.data.local.db.StudyItemEntity
import kotlinx.coroutines.flow.Flow

class NursingRepository(private val db: NursingDatabase) {

    val threadsFlow: Flow<List<ChatThreadEntity>> = db.chatDao().observeThreads()
    val searchHistoryFlow: Flow<List<SearchHistoryEntity>> = db.searchAndSavedDao().observeSearchHistory()
    val savedAnswersFlow: Flow<List<SavedAnswerEntity>> = db.searchAndSavedDao().observeSavedAnswers()
    val knowledgeDocsFlow: Flow<List<KnowledgeDocEntity>> = db.knowledgeDocDao().observeAll()
    val drugsFlow: Flow<List<DrugEntity>> = db.drugDao().observeAll()
    val proceduresFlow: Flow<List<ProcedureEntity>> = db.procedureDao().observeAll()
    val studyItemsFlow: Flow<List<StudyItemEntity>> = db.studyItemDao().observeAll()
    val mcqsFlow: Flow<List<McqQuestionEntity>> = db.mcqDao().observeAll()
    val auditLogsFlow: Flow<List<AuditLogEntity>> = db.auditLogDao().observeAll()

    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>> =
        db.chatDao().observeMessages(threadId)

    suspend fun ensureSeeded() {
        if (db.knowledgeDocDao().getAllOnce().isEmpty()) {
            db.knowledgeDocDao().insertAll(KnowledgeSeedData.initialKnowledgeDocs())
        }
        if (db.drugDao().getAllOnce().isEmpty()) {
            db.drugDao().insertAll(DrugAndProcedureSeedData.initialDrugs())
        }
        if (db.procedureDao().getAllOnce().isEmpty()) {
            db.procedureDao().insertAll(DrugAndProcedureSeedData.initialProcedures())
        }
        if (db.studyItemDao().getAllOnce().isEmpty()) {
            db.studyItemDao().insertAll(StudyAndMcqSeedData.initialStudyItems())
        }
        if (db.mcqDao().getAllOnce().isEmpty()) {
            db.mcqDao().insertAll(StudyAndMcqSeedData.initialMcqs())
        }
    }

    suspend fun upsertThread(thread: ChatThreadEntity) = db.chatDao().upsertThread(thread)
    suspend fun insertMessage(message: ChatMessageEntity) = db.chatDao().insertMessage(message)
    suspend fun getMessagesOnce(threadId: String) = db.chatDao().getMessagesOnce(threadId)
    suspend fun deleteThread(threadId: String) = db.chatDao().deleteThread(threadId)
    suspend fun updateThreadMeta(threadId: String, title: String, updatedAt: Long) =
        db.chatDao().updateThreadMeta(threadId, title, updatedAt)

    suspend fun recordSearch(query: String, category: String = "Nursing AI") {
        if (query.isNotBlank()) {
            db.searchAndSavedDao().insertSearch(
                SearchHistoryEntity(query = query.trim(), category = category)
            )
        }
    }

    suspend fun clearSearchHistory() = db.searchAndSavedDao().clearSearchHistory()

    suspend fun saveAnswer(answer: SavedAnswerEntity) = db.searchAndSavedDao().saveAnswer(answer)
    suspend fun deleteSavedAnswer(id: String) = db.searchAndSavedDao().deleteSavedAnswer(id)

    suspend fun toggleStudyBookmark(id: String, bookmarked: Boolean) =
        db.studyItemDao().setBookmarked(id, bookmarked)

    suspend fun toggleStudyOffline(id: String, saved: Boolean) =
        db.studyItemDao().setOfflineSaved(id, saved)

    suspend fun toggleDrugOffline(id: String, saved: Boolean) =
        db.drugDao().setOfflineSaved(id, saved)

    suspend fun toggleProcedureOffline(id: String, saved: Boolean) =
        db.procedureDao().setOfflineSaved(id, saved)

    suspend fun recordMcqResult(question: McqQuestionEntity, isCorrect: Boolean) {
        val nextBox = if (isCorrect) (question.masteryBox + 1).coerceAtMost(5) else 1
        db.mcqDao().update(
            question.copy(
                masteryBox = nextBox,
                attempts = question.attempts + 1,
                correctCount = question.correctCount + if (isCorrect) 1 else 0
            )
        )
    }

    // Admin Dashboard Operations with Versioning & Audit Logging
    suspend fun adminUpsertKnowledgeDoc(doc: KnowledgeDocEntity, actorRole: String) {
        db.knowledgeDocDao().upsert(doc)
        db.auditLogDao().insert(
            AuditLogEntity(
                actorRole = actorRole,
                action = "Published v${doc.version} (${doc.verificationStatus})",
                entityType = "Knowledge Source",
                entityTitle = doc.titleEn,
                notes = "Updated source: ${doc.sourceTitle}"
            )
        )
    }

    suspend fun adminUpsertDrug(drug: DrugEntity, actorRole: String) {
        db.drugDao().upsert(drug)
        db.auditLogDao().insert(
            AuditLogEntity(
                actorRole = actorRole,
                action = "Updated (${drug.verificationStatus})",
                entityType = "Drug Monograph",
                entityTitle = drug.genericName,
                notes = "Class: ${drug.classification}"
            )
        )
    }

    suspend fun adminUpsertMcq(mcq: McqQuestionEntity, actorRole: String) {
        db.mcqDao().upsert(mcq)
        db.auditLogDao().insert(
            AuditLogEntity(
                actorRole = actorRole,
                action = "Saved MCQ (${mcq.verificationStatus})",
                entityType = "MCQ Bank",
                entityTitle = mcq.subject,
                notes = mcq.questionEn.take(60)
            )
        )
    }

    suspend fun adminLogReport(actorRole: String, title: String, notes: String) {
        db.auditLogDao().insert(
            AuditLogEntity(
                actorRole = actorRole,
                action = "Feedback / Review Flag",
                entityType = "Clinical QA",
                entityTitle = title,
                notes = notes
            )
        )
    }
}

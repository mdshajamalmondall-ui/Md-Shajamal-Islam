package com.example.data.local

import com.example.data.local.db.AuditLogEntity
import com.example.data.local.db.ChatMessageEntity
import com.example.data.local.db.ChatThreadEntity
import com.example.data.local.db.DrugAndProcedureSeedData
import com.example.data.local.db.DrugEntity
import com.example.data.local.db.KnowledgeDocEntity
import com.example.data.local.db.KnowledgeSeedData
import com.example.data.local.db.KnowledgeSeedDataExtra
import com.example.data.local.db.McqQuestionEntity
import com.example.data.local.db.NursingDatabase
import com.example.data.local.db.PersonalNoteEntity
import com.example.data.local.db.ProcedureEntity
import com.example.data.local.db.SavedAnswerEntity
import com.example.data.local.db.SearchHistoryEntity
import com.example.data.local.db.StudyAndMcqSeedData
import com.example.data.local.db.StudyItemEntity
import com.example.data.local.db.UserFileEntity
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
    val personalNotesFlow: Flow<List<PersonalNoteEntity>> = db.personalNoteDao().observeAll()
    val userFilesFlow: Flow<List<UserFileEntity>> = db.userFileDao().observeAll()

    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>> =
        db.chatDao().observeMessages(threadId)

    suspend fun ensureSeeded() {
        val existingDocIds = db.knowledgeDocDao().getAllOnce().map { it.id }.toSet()
        val allDocs = KnowledgeSeedData.initialKnowledgeDocs() + KnowledgeSeedDataExtra.extraKnowledgeDocs()
        val missingDocs = allDocs.filter { it.id !in existingDocIds }
        if (missingDocs.isNotEmpty()) {
            db.knowledgeDocDao().insertAll(missingDocs)
        }

        val existingDrugIds = db.drugDao().getAllOnce().map { it.id }.toSet()
        val missingDrugs = DrugAndProcedureSeedData.initialDrugs().filter { it.id !in existingDrugIds }
        if (missingDrugs.isNotEmpty()) {
            db.drugDao().insertAll(missingDrugs)
        }

        val existingProcIds = db.procedureDao().getAllOnce().map { it.id }.toSet()
        val missingProcs = DrugAndProcedureSeedData.initialProcedures().filter { it.id !in existingProcIds }
        if (missingProcs.isNotEmpty()) {
            db.procedureDao().insertAll(missingProcs)
        }

        val existingStudyIds = db.studyItemDao().getAllOnce().map { it.id }.toSet()
        val missingStudy = StudyAndMcqSeedData.initialStudyItems().filter { it.id !in existingStudyIds }
        if (missingStudy.isNotEmpty()) {
            db.studyItemDao().insertAll(missingStudy)
        }

        val existingMcqIds = db.mcqDao().getAllOnce().map { it.id }.toSet()
        val missingMcqs = StudyAndMcqSeedData.initialMcqs().filter { it.id !in existingMcqIds }
        if (missingMcqs.isNotEmpty()) {
            db.mcqDao().insertAll(missingMcqs)
        }
    }

    suspend fun upsertPersonalNote(note: PersonalNoteEntity) = db.personalNoteDao().upsert(note)
    suspend fun deletePersonalNote(id: String) = db.personalNoteDao().deleteById(id)

    suspend fun upsertUserFile(file: UserFileEntity) = db.userFileDao().upsert(file)
    suspend fun deleteUserFile(id: String) = db.userFileDao().deleteById(id)

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

package com.example.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val languageCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatThreadEntity::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("threadId")]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val sectionsSerialized: String = "", // Title::Body separated by ||SECTION||
    val citationsSerialized: String = "", // Title|Url|Date|Status separated by ||CITE||
    val relatedTopicsSerialized: String = "", // separated by ||TOPIC||
    val emergencyWarning: String = "",
    val imageUri: String? = null,
    val isSampleNoticeShown: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_answers")
data class SavedAnswerEntity(
    @PrimaryKey val id: String,
    val question: String,
    val answerSummary: String,
    val fullFormattedContent: String,
    val citationsSerialized: String = "",
    val category: String = "Nursing AI",
    val languageCode: String = "en",
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "knowledge_docs")
data class KnowledgeDocEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val titleEn: String,
    val titleBn: String,
    val keywords: String,
    val definitionEn: String,
    val definitionBn: String,
    val introductionEn: String,
    val introductionBn: String,
    val causesRiskEn: String,
    val causesRiskBn: String,
    val signsSymptomsEn: String,
    val signsSymptomsBn: String,
    val pathophysiologyEn: String,
    val pathophysiologyBn: String,
    val assessmentEn: String,
    val assessmentBn: String,
    val investigationsEn: String,
    val investigationsBn: String,
    val medicalManagementEn: String,
    val medicalManagementBn: String,
    val nursingManagementEn: String,
    val nursingManagementBn: String,
    val interventionsRationaleEn: String,
    val interventionsRationaleBn: String,
    val complicationsEn: String,
    val complicationsBn: String,
    val preventionEn: String,
    val preventionBn: String,
    val patientEducationEn: String,
    val patientEducationBn: String,
    val nursingNotesEn: String,
    val nursingNotesBn: String,
    val sourceTitle: String,
    val sourceUrl: String,
    val publishedDate: String,
    val verificationStatus: String = "Verified",
    val version: Int = 1,
    val emergencyFlag: String = ""
)

@Entity(tableName = "drugs")
data class DrugEntity(
    @PrimaryKey val id: String,
    val genericName: String,
    val brandNames: String,
    val classification: String,
    val indications: String,
    val mechanismOfAction: String,
    val dosageReference: String,
    val route: String,
    val contraindications: String,
    val sideEffects: String,
    val adverseReactions: String,
    val nursingResponsibilities: String,
    val drugInteractions: String,
    val patientCounseling: String,
    val storageInfo: String,
    val sourceTitle: String,
    val updatedDate: String,
    val verificationStatus: String = "Verified",
    val isHighAlert: Boolean = false,
    val isOfflineSaved: Boolean = true
)

@Entity(tableName = "procedures")
data class ProcedureEntity(
    @PrimaryKey val id: String,
    val titleEn: String,
    val titleBn: String,
    val category: String,
    val supervisedTrainingRequired: Boolean = true,
    val definition: String,
    val purpose: String,
    val equipment: String,
    val preparation: String,
    val steps: String,
    val nursingResponsibilities: String,
    val infectionControl: String,
    val complications: String,
    val documentation: String,
    val sourceTitle: String,
    val updatedDate: String,
    val verificationStatus: String = "Verified",
    val drawableKey: String? = null,
    val isOfflineSaved: Boolean = true
)

@Entity(tableName = "study_items")
data class StudyItemEntity(
    @PrimaryKey val id: String,
    val itemType: String, // NOTE, BOOK, SHORT_QA, BROAD_QA, VIVA, CASE_STUDY, ASSIGNMENT, CARE_PLAN, NANDA, DEFINITION, CAREER, EXAM, SCHOLARSHIP
    val subject: String,
    val titleEn: String,
    val titleBn: String,
    val bodyEn: String,
    val bodyBn: String,
    val sourceTitle: String,
    val sourceUrl: String = "",
    val updatedDate: String,
    val verificationStatus: String = "Verified",
    val isBookmarked: Boolean = false,
    val isOfflineSaved: Boolean = true
)

@Entity(tableName = "mcq_questions")
data class McqQuestionEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val difficulty: String = "Standard",
    val questionEn: String,
    val questionBn: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int,
    val rationaleEn: String,
    val rationaleBn: String,
    val sourceTitle: String,
    val verificationStatus: String = "Verified",
    val masteryBox: Int = 1, // Spaced repetition box 1..5
    val attempts: Int = 0,
    val correctCount: Int = 0
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorRole: String,
    val action: String,
    val entityType: String,
    val entityTitle: String,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_notes")
data class PersonalNoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val tag: String = "Nursing Note",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_files")
data class UserFileEntity(
    @PrimaryKey val id: String,
    val title: String,
    val folder: String = "Study Files",
    val uriString: String,
    val mimeType: String = "*/*",
    val sizeBytes: Long = 0L,
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

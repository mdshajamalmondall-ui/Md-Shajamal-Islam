package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiNursingAiRepository
import com.example.data.ai.LocalGroundedAiRepository
import com.example.data.ai.NursingAiRepository
import com.example.data.ai.NursingAiResponse
import com.example.data.ai.NursingCitation
import com.example.data.ai.NursingSection
import com.example.data.ai.SmartAiToolType
import com.example.data.local.AppLanguage
import com.example.data.local.NursingRepository
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.UserRole
import com.example.data.local.db.AuditLogEntity
import com.example.data.local.db.ChatMessageEntity
import com.example.data.local.db.ChatThreadEntity
import com.example.data.local.db.DrugEntity
import com.example.data.local.db.KnowledgeDocEntity
import com.example.data.local.db.McqQuestionEntity
import com.example.data.local.db.NursingDatabase
import com.example.data.local.db.PersonalNoteEntity
import com.example.data.local.db.ProcedureEntity
import com.example.data.local.db.SavedAnswerEntity
import com.example.data.local.db.SearchHistoryEntity
import com.example.data.local.db.StudyItemEntity
import com.example.data.local.db.UserFileEntity
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatErrorState(
    val query: String,
    val message: String
)

sealed interface AiGenerationState {
    data object Idle : AiGenerationState
    data object Loading : AiGenerationState
    data class Success(val response: NursingAiResponse) : AiGenerationState
    data class Error(val message: String) : AiGenerationState
}

private const val VM_TAG = "NursingViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
class NursingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NursingDatabase.getInstance(application)
    private val repository = NursingRepository(database)
    private val prefsRepo = UserPreferencesRepository(application)
    private val aiRepository: NursingAiRepository = GeminiNursingAiRepository(application)
    private val offlineFallbackRepository: NursingAiRepository = LocalGroundedAiRepository(database)

    val preferences: StateFlow<UserPreferences> = prefsRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    val threads: StateFlow<List<ChatThreadEntity>> = repository.threadsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.searchHistoryFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val savedAnswers: StateFlow<List<SavedAnswerEntity>> = repository.savedAnswersFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val knowledgeDocs: StateFlow<List<KnowledgeDocEntity>> = repository.knowledgeDocsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val drugs: StateFlow<List<DrugEntity>> = repository.drugsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val procedures: StateFlow<List<ProcedureEntity>> = repository.proceduresFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val studyItems: StateFlow<List<StudyItemEntity>> = repository.studyItemsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val mcqs: StateFlow<List<McqQuestionEntity>> = repository.mcqsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val personalNotes: StateFlow<List<PersonalNoteEntity>> = repository.personalNotesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val userFiles: StateFlow<List<UserFileEntity>> = repository.userFilesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _activeThreadId = MutableStateFlow<String?>(null)
    val activeThreadId: StateFlow<String?> = _activeThreadId.asStateFlow()

    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeThreadId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    private val _streamingStatus = MutableStateFlow<String?>(null)
    val streamingStatus: StateFlow<String?> = _streamingStatus.asStateFlow()

    private val _chatError = MutableStateFlow<ChatErrorState?>(null)
    val chatError: StateFlow<ChatErrorState?> = _chatError.asStateFlow()

    private val _attachedImageUri = MutableStateFlow<Uri?>(null)
    val attachedImageUri: StateFlow<Uri?> = _attachedImageUri.asStateFlow()

    private val _assistantState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val assistantState: StateFlow<AiGenerationState> = _assistantState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setLanguage(language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun updateUserProfile(
        name: String,
        email: String,
        phone: String,
        institution: String,
        studentBatch: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            prefsRepo.setUserProfile(name, email, phone, institution, studentBatch, role)
        }
    }

    fun updateProfilePhoto(uri: Uri?) {
        viewModelScope.launch {
            prefsRepo.setProfilePhotoUri(uri?.toString().orEmpty())
        }
    }

    fun addOrUpdateNote(id: String? = null, title: String, content: String, tag: String = "Nursing Note") {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            repository.upsertPersonalNote(
                PersonalNoteEntity(
                    id = id ?: UUID.randomUUID().toString(),
                    title = title.trim().ifBlank { "Untitled Note" },
                    content = content.trim(),
                    tag = tag.trim().ifBlank { "Nursing Note" },
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch { repository.deletePersonalNote(id) }
    }

    fun addUserFile(
        title: String,
        folder: String,
        uriString: String,
        mimeType: String,
        sizeBytes: Long,
        notes: String = ""
    ) {
        if (uriString.isBlank()) return
        viewModelScope.launch {
            repository.upsertUserFile(
                UserFileEntity(
                    id = UUID.randomUUID().toString(),
                    title = title.trim().ifBlank { "Nursing File" },
                    folder = folder.trim().ifBlank { "Study Files" },
                    uriString = uriString,
                    mimeType = mimeType.ifBlank { "*/*" },
                    sizeBytes = sizeBytes,
                    notes = notes.trim(),
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteUserFile(id: String) {
        viewModelScope.launch { repository.deleteUserFile(id) }
    }

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch { prefsRepo.setDailyGoal(goal) }
    }

    fun attachImage(uri: Uri?) {
        _attachedImageUri.value = uri
    }

    fun startNewChat() {
        _activeThreadId.value = null
        _attachedImageUri.value = null
        _chatError.value = null
        _streamingStatus.value = null
    }

    fun selectThread(threadId: String) {
        _activeThreadId.value = threadId
        _chatError.value = null
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            if (_activeThreadId.value == threadId) {
                _activeThreadId.value = null
            }
            repository.deleteThread(threadId)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }

    fun dismissChatError() {
        _chatError.value = null
    }

    fun retryLastChatError(useOfflineFallback: Boolean = false) {
        val error = _chatError.value ?: return
        sendChatQuery(error.query, useOfflineFallback = useOfflineFallback)
    }

    fun sendChatQuery(rawQuery: String, useOfflineFallback: Boolean = false) {
        val query = rawQuery.trim()
        val imageUri = _attachedImageUri.value
        if (query.isBlank() && imageUri == null) return

        val effectiveQuery = query.ifBlank { "Explain this nursing study image" }
        val lang = preferences.value.language

        viewModelScope.launch {
            _isChatGenerating.value = true
            _chatError.value = null
            _streamingStatus.value = null
            try {
                val threadId = _activeThreadId.value ?: UUID.randomUUID().toString().also { newId ->
                    repository.upsertThread(
                        ChatThreadEntity(
                            id = newId,
                            title = effectiveQuery.take(48),
                            languageCode = lang.code
                        )
                    )
                    _activeThreadId.value = newId
                }

                val existingHistory = repository.getMessagesOnce(threadId)
                val lastMsg = existingHistory.lastOrNull()
                val isRetryingSameTurn = lastMsg?.role == "user" && lastMsg.content == effectiveQuery

                if (!isRetryingSameTurn) {
                    repository.recordSearch(effectiveQuery, "Nursing AI")
                    repository.insertMessage(
                        ChatMessageEntity(
                            id = UUID.randomUUID().toString(),
                            threadId = threadId,
                            role = "user",
                            content = effectiveQuery,
                            imageUri = imageUri?.toString()
                        )
                    )
                }
                _attachedImageUri.value = null

                val engine = if (useOfflineFallback) offlineFallbackRepository else aiRepository
                val aiResponse = engine.askNursingQuestion(
                    query = effectiveQuery,
                    history = existingHistory.filterNot { isRetryingSameTurn && it.id == lastMsg?.id },
                    language = lang,
                    attachedImageLabel = imageUri?.lastPathSegment,
                    attachedImageUri = imageUri,
                    onStreamingUpdate = { preview -> _streamingStatus.value = preview }
                )

                repository.insertMessage(
                    ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        threadId = threadId,
                        role = "assistant",
                        content = aiResponse.summary,
                        sectionsSerialized = serializeSections(aiResponse.sections),
                        citationsSerialized = serializeCitations(aiResponse.citations),
                        relatedTopicsSerialized = aiResponse.relatedTopics.joinToString("||TOPIC||"),
                        emergencyWarning = aiResponse.emergencyWarning,
                        isSampleNoticeShown = !aiResponse.isLiveGemini
                    )
                )

                val threadTitle = existingHistory.firstOrNull { it.role == "user" }?.content?.take(48)
                    ?: effectiveQuery.take(48)
                repository.updateThreadMeta(threadId, threadTitle, System.currentTimeMillis())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(VM_TAG, "Chat query failed: $effectiveQuery", e)
                _chatError.value = ChatErrorState(
                    query = effectiveQuery,
                    message = e.localizedMessage ?: "Failed to generate nursing answer."
                )
            } finally {
                _isChatGenerating.value = false
                _streamingStatus.value = null
            }
        }
    }

    fun saveChatMessage(question: String, msg: ChatMessageEntity) {
        viewModelScope.launch {
            val sections = parseSections(msg.sectionsSerialized)
            val fullText = buildString {
                appendLine(msg.content)
                sections.forEach {
                    appendLine("\n${it.title}")
                    appendLine(it.body)
                }
            }
            repository.saveAnswer(
                SavedAnswerEntity(
                    id = msg.id,
                    question = question,
                    answerSummary = msg.content,
                    fullFormattedContent = fullText,
                    citationsSerialized = msg.citationsSerialized,
                    languageCode = preferences.value.language.code
                )
            )
        }
    }

    fun deleteSavedAnswer(id: String) {
        viewModelScope.launch { repository.deleteSavedAnswer(id) }
    }

    fun generateCarePlan(patientCase: String, useOfflineFallback: Boolean = false) {
        if (patientCase.isBlank()) {
            _assistantState.value = AiGenerationState.Error("Please enter patient case details first.")
            return
        }
        viewModelScope.launch {
            _assistantState.value = AiGenerationState.Loading
            try {
                val engine = if (useOfflineFallback) offlineFallbackRepository else aiRepository
                val res = engine.generateCarePlan(patientCase.trim(), preferences.value.language)
                _assistantState.value = AiGenerationState.Success(res)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(VM_TAG, "Care plan generation failed", e)
                _assistantState.value = AiGenerationState.Error(e.localizedMessage ?: "Failed to generate care plan.")
            }
        }
    }

    fun runSmartTool(toolType: SmartAiToolType, input: String, useOfflineFallback: Boolean = false) {
        val query = input.trim().ifBlank {
            if (preferences.value.language == AppLanguage.BENGALI) toolType.samplePromptBn else toolType.samplePromptEn
        }
        viewModelScope.launch {
            _assistantState.value = AiGenerationState.Loading
            try {
                val engine = if (useOfflineFallback) offlineFallbackRepository else aiRepository
                val res = engine.runSmartTool(toolType, query, preferences.value.language)
                _assistantState.value = AiGenerationState.Success(res)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(VM_TAG, "Smart tool execution failed", e)
                _assistantState.value = AiGenerationState.Error(e.localizedMessage ?: "Smart tool failed.")
            }
        }
    }

    fun recordMcqAnswer(question: McqQuestionEntity, isCorrect: Boolean) {
        viewModelScope.launch {
            repository.recordMcqResult(question, isCorrect)
            prefsRepo.incrementCompletedQuestions(1)
        }
    }

    fun toggleStudyBookmark(item: StudyItemEntity) {
        viewModelScope.launch { repository.toggleStudyBookmark(item.id, !item.isBookmarked) }
    }

    fun toggleStudyOffline(item: StudyItemEntity) {
        viewModelScope.launch { repository.toggleStudyOffline(item.id, !item.isOfflineSaved) }
    }

    fun toggleDrugOffline(drug: DrugEntity) {
        viewModelScope.launch { repository.toggleDrugOffline(drug.id, !drug.isOfflineSaved) }
    }

    fun toggleProcedureOffline(proc: ProcedureEntity) {
        viewModelScope.launch { repository.toggleProcedureOffline(proc.id, !proc.isOfflineSaved) }
    }

    fun adminSaveKnowledgeTopic(
        titleEn: String,
        titleBn: String,
        subject: String,
        definitionEn: String,
        nursingManagementEn: String,
        sourceTitle: String,
        verificationStatus: String
    ) {
        viewModelScope.launch {
            val doc = KnowledgeDocEntity(
                id = "kb_custom_${System.currentTimeMillis()}",
                subject = subject,
                titleEn = titleEn,
                titleBn = titleBn.ifBlank { titleEn },
                keywords = "$titleEn $titleBn $subject".lowercase(),
                definitionEn = definitionEn,
                definitionBn = definitionEn,
                introductionEn = "Reviewer-managed educational clinical topic.",
                introductionBn = "রিভিউয়ার অনুমোদিত শিক্ষামূলক ক্লিনিক্যাল টপিক।",
                causesRiskEn = "Refer to institutional clinical guidelines.",
                causesRiskBn = "প্রতিষ্ঠানের ক্লিনিক্যাল গাইডলাইন অনুসরণ করুন।",
                signsSymptomsEn = "Monitor baseline and acute clinical cues.",
                signsSymptomsBn = "মূল ও তীব্র ক্লিনিক্যাল লক্ষণ পর্যবেক্ষণ করুন।",
                pathophysiologyEn = "Evidence-based clinical mechanism.",
                pathophysiologyBn = "প্রমাণ-ভিত্তিক ক্লিনিক্যাল প্রক্রিয়া।",
                assessmentEn = "Complete ABCDE & focused system assessment.",
                assessmentBn = "ABCDE ও সিস্টেম-ভিত্তিক মূল্যায়ন করুন।",
                investigationsEn = "Order-specific diagnostic labs and imaging.",
                investigationsBn = "নির্দেশিত ল্যাব ও ডায়াগনস্টিক পরীক্ষা।",
                medicalManagementEn = "Physician-prescribed evidence-based pharmacotherapy.",
                medicalManagementBn = "চিকিৎসকের নির্দেশিত প্রমাণ-ভিত্তিক চিকিৎসা।",
                nursingManagementEn = nursingManagementEn,
                nursingManagementBn = nursingManagementEn,
                interventionsRationaleEn = "Prioritize patient safety, vitals, and teach-back education.",
                interventionsRationaleBn = "রোগীর নিরাপত্তা, ভাইটাল সাইন ও শিক্ষাকে অগ্রাধিকার দিন।",
                complicationsEn = "Early recognition prevents escalation.",
                complicationsBn = "প্রাথমিক শনাক্তকরণ জটিলতা প্রতিরোধ করে।",
                preventionEn = "Follow standard infection control and preventive screening.",
                preventionBn = "সংক্রমণ প্রতিরোধ ও স্ক্রিনিং মেনে চলুন।",
                patientEducationEn = "Verify patient comprehension using teach-back.",
                patientEducationBn = "টিচ-ব্যাক পদ্ধতিতে রোগীর বোঝা যাচাই করুন।",
                nursingNotesEn = "Approved by ${preferences.value.userRole.labelEn}.",
                nursingNotesBn = "${preferences.value.userRole.labelEn} কর্তৃক অনুমোদিত।",
                sourceTitle = sourceTitle.ifBlank { "Clinical Nursing Editorial Board" },
                sourceUrl = "https://www.who.int",
                publishedDate = "2026-11-20",
                verificationStatus = verificationStatus,
                version = 1
            )
            repository.adminUpsertKnowledgeDoc(doc, preferences.value.userRole.labelEn)
        }
    }

    fun adminSaveDrug(generic: String, brands: String, drugClass: String, dosage: String, nursing: String) {
        viewModelScope.launch {
            repository.adminUpsertDrug(
                DrugEntity(
                    id = "drug_custom_${System.currentTimeMillis()}",
                    genericName = generic,
                    brandNames = brands,
                    classification = drugClass,
                    indications = "Clinician-reviewed indication entry.",
                    mechanismOfAction = "Refer to approved pharmacopoeia.",
                    dosageReference = dosage,
                    route = "Per clinical order",
                    contraindications = "Known hypersensitivity.",
                    sideEffects = "Monitor patient tolerance.",
                    adverseReactions = "Report ADRs per institutional protocol.",
                    nursingResponsibilities = nursing,
                    drugInteractions = "Verify drug-drug interactions before dosing.",
                    patientCounseling = "Take as directed; report unusual symptoms.",
                    storageInfo = "Store per manufacturer labeling.",
                    sourceTitle = "Admin Clinical Formulary Review",
                    updatedDate = "2026-11-20",
                    verificationStatus = "Reviewer Verified"
                ),
                preferences.value.userRole.labelEn
            )
        }
    }

    fun adminSaveMcq(subject: String, question: String, a: String, b: String, c: String, d: String, correctIdx: Int, rationale: String) {
        viewModelScope.launch {
            repository.adminUpsertMcq(
                McqQuestionEntity(
                    id = "mcq_custom_${System.currentTimeMillis()}",
                    subject = subject,
                    questionEn = question,
                    questionBn = question,
                    optionA = a,
                    optionB = b,
                    optionC = c,
                    optionD = d,
                    correctIndex = correctIdx.coerceIn(0, 3),
                    rationaleEn = rationale,
                    rationaleBn = rationale,
                    sourceTitle = "Admin Verified Question Bank",
                    verificationStatus = "Verified"
                ),
                preferences.value.userRole.labelEn
            )
        }
    }

    fun submitClinicalFeedback(title: String, notes: String) {
        viewModelScope.launch {
            repository.adminLogReport(preferences.value.userRole.labelEn, title, notes)
        }
    }

    companion object {
        fun serializeSections(sections: List<NursingSection>): String =
            sections.joinToString("||SECTION||") { "${it.title}::${it.body}" }

        fun parseSections(serialized: String): List<NursingSection> {
            if (serialized.isBlank()) return emptyList()
            return serialized.split("||SECTION||").mapNotNull { chunk ->
                val idx = chunk.indexOf("::")
                if (idx > 0) NursingSection(chunk.substring(0, idx), chunk.substring(idx + 2)) else null
            }
        }

        fun serializeCitations(citations: List<NursingCitation>): String =
            citations.joinToString("||CITE||") { "${it.title}|${it.url}|${it.publishedDate}|${it.verificationStatus}" }

        fun parseCitations(serialized: String): List<NursingCitation> {
            if (serialized.isBlank()) return emptyList()
            return serialized.split("||CITE||").mapNotNull { chunk ->
                val parts = chunk.split("|")
                if (parts.size >= 4) NursingCitation(parts[0], parts[1], parts[2], parts[3]) else null
            }
        }

        fun parseRelatedTopics(serialized: String): List<String> =
            if (serialized.isBlank()) emptyList() else serialized.split("||TOPIC||").filter { it.isNotBlank() }
    }
}

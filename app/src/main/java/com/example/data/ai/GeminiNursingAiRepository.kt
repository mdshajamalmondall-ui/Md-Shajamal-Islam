package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.data.local.db.ChatMessageEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class GeminiNursingAiRepository(
    private val context: Context
) : NursingAiRepository {

    private val nursingResponseSchema: Schema by lazy {
        Schema.obj(
            properties = mapOf(
                "summary" to Schema.string(
                    description = "Concise direct headline or answer summary tailored to the user's question and requested length"
                ),
                "sections" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "title" to Schema.string(
                                description = "Section heading (e.g., 1. Definition, Indications, Nursing Management, Contraindications)"
                            ),
                            "body" to Schema.string(
                                description = "Clear, evidence-based clinical bullet points or explanation in proper nursing terminology"
                            )
                        )
                    ),
                    description = "Structured clinical sections. Include only sections relevant to the question."
                ),
                "citations" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "title" to Schema.string(description = "Reference guideline or textbook name"),
                            "url" to Schema.string(description = "Official institutional root URL such as https://www.who.int or https://www.icn.ch"),
                            "publishedDate" to Schema.string(description = "Year or YYYY-MM-DD"),
                            "verificationStatus" to Schema.string(description = "Source status, e.g. Clinical Guideline or Nursing Reference")
                        )
                    ),
                    description = "Authoritative nursing & medical reference sources"
                ),
                "relatedTopics" to Schema.array(
                    items = Schema.string(),
                    description = "3 or 4 short follow-up nursing queries the student may tap next"
                ),
                "emergencyWarning" to Schema.string(
                    description = "Urgent red-flag clinical alert ONLY when emergency symptoms or high-alert medications are involved; otherwise empty string"
                )
            ),
            optionalProperties = listOf("citations", "relatedTopics", "emergencyWarning")
        )
    }

    private val nursingModel by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = nursingResponseSchema
        }
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = config,
            systemInstruction = content {
                text(NURSING_SYSTEM_INSTRUCTION)
            }
        )
    }

    override suspend fun askNursingQuestion(
        query: String,
        history: List<ChatMessageEntity>,
        language: AppLanguage,
        attachedImageLabel: String?,
        attachedImageUri: Uri?,
        onStreamingUpdate: ((String) -> Unit)?
    ): NursingAiResponse {
        val effectiveLang = detectEffectiveLanguage(query, language)
        ensureNetworkOrThrow(effectiveLang)

        val imageBitmap = if (attachedImageUri != null) {
            decodeScaledBitmap(attachedImageUri)
        } else {
            null
        }

        val chatHistoryContents = buildChatHistory(history)
        val langDirective = if (effectiveLang == AppLanguage.BENGALI) {
            "Preferred output language: BENGALI (বাংলা). Write explanations clearly in Bengali while keeping essential international medical/drug terms in English or parentheses (e.g., SpO2, BP, IV, MgSO4, NANDA-I)."
        } else {
            "Preferred output language: ENGLISH. Write in clear, evidence-based clinical nursing English."
        }

        val userPromptText = buildString {
            appendLine(langDirective)
            if (imageBitmap != null) {
                appendLine("The user also attached a clinical/study image (${attachedImageLabel ?: "image"}). Analyze the image carefully alongside the question.")
            }
            appendLine("Question: $query")
        }

        val currentTurnContent = content("user") {
            if (imageBitmap != null) {
                image(imageBitmap)
            }
            text(userPromptText)
        }

        return try {
            val rawJson = withTimeout(TIMEOUT_MS) {
                val buffer = StringBuilder()
                val chat = nursingModel.startChat(history = chatHistoryContents)
                chat.sendMessageStream(currentTurnContent).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        buffer.append(piece)
                        onStreamingUpdate?.invoke(
                            extractStreamingPreview(buffer.toString(), effectiveLang)
                        )
                    }
                }
                buffer.toString()
            }

            if (rawJson.isBlank()) {
                throw IllegalStateException(
                    bi(
                        effectiveLang,
                        "Gemini AI returned an empty response. Please try again.",
                        "Gemini AI থেকে কোনো উত্তর পাওয়া যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।"
                    )
                )
            }

            parseStructuredResponse(rawJson, effectiveLang)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini nursing query timed out after 60s", e)
            throw IOException(
                bi(
                    effectiveLang,
                    "Response timed out after 60 seconds. Check your internet connection and retry.",
                    "উত্তর তৈরি হতে ৬০ সেকেন্ডের বেশি সময় লেগেছে। ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।"
                ),
                e
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed for query: $query", e)
            throw IOException(formatFriendlyError(e, effectiveLang), e)
        }
    }

    override suspend fun generateCarePlan(
        patientCase: String,
        language: AppLanguage
    ): NursingAiResponse {
        val effectiveLang = detectEffectiveLanguage(patientCase, language)
        ensureNetworkOrThrow(effectiveLang)

        val prompt = buildString {
            appendLine(
                if (effectiveLang == AppLanguage.BENGALI) {
                    "Output language: BENGALI (বাংলা) with standard NANDA-I / NIC / NOC terms in English."
                } else {
                    "Output language: ENGLISH."
                }
            )
            appendLine("Create a complete, clinical Nursing Care Plan (ADPIE format) for this patient case:")
            appendLine(patientCase)
            appendLine("Required sections: 1. Patient Assessment & Priority Cues, 2. Nursing Diagnosis (NANDA-I format with 'related to' and 'as evidenced by'), 3. Goals & Expected Outcomes (NOC - measurable & time-bound), 4. Nursing Interventions (NIC - prioritized), 5. Scientific Rationales, 6. Evaluation & Red Flags.")
        }

        return runStructuredPrompt(prompt, effectiveLang)
    }

    override suspend fun runSmartTool(
        toolType: SmartAiToolType,
        input: String,
        language: AppLanguage
    ): NursingAiResponse {
        val effectiveLang = detectEffectiveLanguage(input, language)
        ensureNetworkOrThrow(effectiveLang)

        val toolDirective = when (toolType) {
            SmartAiToolType.TUTOR -> "Act as a senior Nursing Educator. Break down the concept clearly with pathophysiology, clinical reasoning, and ward tips."
            SmartAiToolType.CASE_EXPLAINER -> "Analyze the clinical case using ABCDE prioritization, abnormal cue identification, immediate bedside actions, and expected orders."
            SmartAiToolType.TRANSLATOR -> "Translate and explain every medical/nursing term accurately between English and Bengali, including clinical meaning and bedside action."
            SmartAiToolType.SUMMARIZER -> "Summarize the topic into high-yield exam revision bullets, normal lab values, and memory hooks."
            SmartAiToolType.MCQ_GEN -> "Generate 3 to 5 BNMC/NCLEX-style clinical scenario MCQs with options A-D, the correct answer clearly marked, and concise clinical rationales."
            SmartAiToolType.VIVA_PRACTICE -> "Create realistic Nursing Viva examiner questions paired with crisp model answers and common follow-up traps."
            SmartAiToolType.ASSIGNMENT -> "Provide a structured academic nursing assignment/study guide outline with introduction, clinical body points, EBP focus, and conclusion."
            SmartAiToolType.SCENARIO_PRACTICE -> "Simulate a bedside ward scenario with immediate assessment, priority intervention, rationale, and SBAR communication."
        }

        val prompt = buildString {
            appendLine(
                if (effectiveLang == AppLanguage.BENGALI) {
                    "Output language: BENGALI (বাংলা) with key medical terminology preserved in English."
                } else {
                    "Output language: ENGLISH."
                }
            )
            appendLine("Mode: ${toolType.titleEn}")
            appendLine("Instruction: $toolDirective")
            appendLine("User input: $input")
        }

        return runStructuredPrompt(prompt, effectiveLang)
    }

    private suspend fun runStructuredPrompt(
        prompt: String,
        language: AppLanguage
    ): NursingAiResponse {
        return try {
            val rawJson = withTimeout(TIMEOUT_MS) {
                val buffer = StringBuilder()
                nursingModel.generateContentStream(prompt).collect { chunk ->
                    buffer.append(chunk.text.orEmpty())
                }
                buffer.toString()
            }
            if (rawJson.isBlank()) {
                throw IllegalStateException(
                    bi(
                        language,
                        "Gemini AI returned an empty response.",
                        "Gemini AI থেকে কোনো উত্তর পাওয়া যায়নি।"
                    )
                )
            }
            parseStructuredResponse(rawJson, language)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini tool generation timed out", e)
            throw IOException(
                bi(
                    language,
                    "Generation timed out after 60 seconds. Please try again.",
                    "উত্তর তৈরি হতে ৬০ সেকেন্ডের বেশি সময় লেগেছে। আবার চেষ্টা করুন।"
                ),
                e
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini tool generation failed", e)
            throw IOException(formatFriendlyError(e, language), e)
        }
    }

    private fun buildChatHistory(history: List<ChatMessageEntity>): List<Content> {
        return history.takeLast(8).mapNotNull { msg ->
            val role = when (msg.role) {
                "user" -> "user"
                "assistant" -> "model"
                else -> return@mapNotNull null
            }
            val textBody = if (role == "model") {
                val sections = NursingSectionSerializer.parseSections(msg.sectionsSerialized)
                buildString {
                    appendLine(msg.content)
                    sections.take(4).forEach {
                        appendLine("${it.title}: ${it.body.take(260)}")
                    }
                }.trim()
            } else {
                msg.content
            }
            if (textBody.isBlank()) null else content(role) { text(textBody) }
        }
    }

    private suspend fun decodeScaledBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            val maxDim = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
            var sampleSize = 1
            while (maxDim / sampleSize > MAX_IMAGE_DIM) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode attached image bitmap from URI: $uri", e)
            null
        }
    }

    private fun ensureNetworkOrThrow(language: AppLanguage) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(network)
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        if (!hasInternet) {
            throw IOException(
                bi(
                    language,
                    "No internet connection detected. Connect to internet for Live Gemini AI or tap 'Use Offline Reference' below.",
                    "ইন্টারনেট সংযোগ নেই। লাইভ Gemini AI ব্যবহার করতে ইন্টারনেট চালু করুন অথবা নিচের 'অফলাইন রেফারেন্স' চাপুন।"
                )
            )
        }
    }

    private fun detectEffectiveLanguage(query: String, pref: AppLanguage): AppLanguage {
        return if (query.any { it in '\u0980'..'\u09FF' }) AppLanguage.BENGALI else pref
    }

    private fun formatFriendlyError(error: Exception, language: AppLanguage): String {
        val raw = error.localizedMessage.orEmpty()
        return when {
            raw.contains("403") || raw.contains("AppCheck", ignoreCase = true) -> bi(
                language,
                "App Check verification error ($raw). Please retry or verify network/device time.",
                "App Check যাচাইকরণ ত্রুটি ($raw)। অনুগ্রহ করে পুনরায় চেষ্টা করুন।"
            )
            raw.contains("429") || raw.contains("quota", ignoreCase = true) -> bi(
                language,
                "AI service quota is temporarily busy. Please wait a moment and try again.",
                "AI সার্ভারে সাময়িক চাপ রয়েছে। কিছুক্ষণ পর আবার চেষ্টা করুন।"
            )
            raw.isNotBlank() -> raw
            else -> bi(
                language,
                "Unable to reach Live Gemini AI right now. Please try again or use offline reference.",
                "এই মুহূর্তে লাইভ Gemini AI-এর সাথে সংযোগ করা যাচ্ছে না। আবার চেষ্টা করুন অথবা অফলাইন রেফারেন্স দেখুন।"
            )
        }
    }

    private fun extractStreamingPreview(partialJson: String, language: AppLanguage): String {
        val summaryMarker = "\"summary\":\""
        val start = partialJson.indexOf(summaryMarker)
        if (start >= 0) {
            val after = partialJson.substring(start + summaryMarker.length)
            val end = after.indexOf('"')
            val clean = (if (end >= 0) after.substring(0, end) else after)
                .replace("\\n", " ")
                .trim()
            if (clean.length >= 6) return clean
        }
        return bi(language, "Generating evidence-based nursing answer...", "তথ্য-সমৃদ্ধ নার্সিং উত্তর তৈরি হচ্ছে...")
    }

    companion object {
        private const val TAG = "GeminiNursingAI"
        private const val MODEL_NAME = "gemini-3.8-flash"
        private const val TIMEOUT_MS = 60_000L
        private const val MAX_IMAGE_DIM = 1024

        private const val NURSING_SYSTEM_INSTRUCTION = """
You are NURSING AI, an expert Nursing Educator, Clinical Pharmacology Specialist, and Healthcare Reference Assistant serving nursing students (Diploma, B.Sc., M.Sc., BNMC/NCLEX), registered nurses, and medical educators.

Your goal is to answer ANY nursing, medical, anatomy, physiology, pathophysiology, pharmacology/drug, procedure, microbiology, nutrition, maternal-child, pediatric, psychiatric, critical care, or clinical terminology question accurately, clearly, and in an immediately useful format — matching the depth and style the user asked for.

RULES FOR RESPONSE DEPTH & FORMAT:
1. ADAPT TO USER INTENT:
   - If the user asks a brief/specific question (e.g., "definition of pain", "normal BP", "what is tachycardia?", "indication of paracetamol", "সংক্ষেপে বলো", "just contraindications"): give a direct, crisp summary and ONLY 1–3 relevant sections. Do NOT pad with unnecessary sections.
   - If the user asks broadly about a condition, disease, nursing care, or asks for detailed/comprehensive notes ("Pneumonia", "Hypertension management", "DKA", "বিস্তারিত", "full notes"): provide thorough, exam-ready and ward-ready sections from the 15-point nursing format (only including sections genuinely relevant to the topic):
     Definition, Introduction, Causes / Risk Factors, Signs & Symptoms, Pathophysiology, Assessment, Investigations, Medical Management, Nursing Management, Nursing Interventions & Rationale, Complications, Prevention, Patient Education, Important Nursing Notes.
   - If the user asks about ANY MEDICINE / DRUG (generic or brand name like Paracetamol, Napa, Ceftriaxone, Metformin, Insulin, Lasix, Adrenaline, MgSO4, Omeprazole, etc.): include Drug Overview & Classification, Indications, Mechanism of Action, Standard Dosage & Route (with clinical verification reminder), Contraindications, Side Effects & Adverse Reactions, and Priority Nursing Responsibilities & Patient Counseling.
2. LANGUAGE:
   - Support English, Bengali (বাংলা), and Banglish queries.
   - Output in the requested language directive. When answering in Bengali, use clear, natural clinical Bengali while preserving standard medical/drug terms (e.g., SpO2, BP, IV, ECG, NANDA-I, Drug Names) in English or parentheses.
3. CLINICAL SAFETY:
   - Include a concise emergencyWarning ONLY if the query involves urgent red-flag symptoms (e.g., chest pain, anaphylaxis, respiratory distress, sepsis, status epilepticus, severe hemorrhage) or High-Alert medications (e.g., Insulin, Heparin, KCl, MgSO4, Epinephrine, Digoxin). Otherwise leave emergencyWarning empty "".
   - Never fabricate specific patient data or claim to replace licensed physicians or local hospital protocols.
4. CITATIONS & FOLLOW-UPS:
   - Cite standard nursing/clinical authorities (e.g., WHO Guidelines, ICN, Potter & Perry's Fundamentals of Nursing, Brunner & Suddarth's Textbook of Medical-Surgical Nursing, BNMC Curriculum, BNF / FDA Prescribing Information) with official root URLs only.
   - Provide 3–4 short, relevant follow-up questions in relatedTopics in the same language as the answer.
"""

        fun parseStructuredResponse(jsonString: String, language: AppLanguage): NursingAiResponse {
            val json = JSONObject(jsonString)
            val summary = json.optString("summary").trim().ifBlank {
                bi(language, "Nursing Clinical Answer", "নার্সিং ক্লিনিক্যাল উত্তর")
            }

            val sectionsList = mutableListOf<NursingSection>()
            val sectionsArray = json.optJSONArray("sections")
            if (sectionsArray != null) {
                for (i in 0 until sectionsArray.length()) {
                    val obj = sectionsArray.optJSONObject(i) ?: continue
                    val title = obj.optString("title").trim()
                    val body = obj.optString("body").trim()
                    if (title.isNotEmpty() && body.isNotEmpty()) {
                        sectionsList.add(NursingSection(title, body))
                    }
                }
            }

            val citationsList = mutableListOf<NursingCitation>()
            val citationsArray = json.optJSONArray("citations")
            if (citationsArray != null) {
                for (i in 0 until citationsArray.length()) {
                    val obj = citationsArray.optJSONObject(i) ?: continue
                    val title = obj.optString("title").trim()
                    if (title.isNotEmpty()) {
                        citationsList.add(
                            NursingCitation(
                                title = title,
                                url = obj.optString("url", "https://www.who.int").ifBlank { "https://www.who.int" },
                                publishedDate = obj.optString("publishedDate", "2025").ifBlank { "2025" },
                                verificationStatus = obj.optString("verificationStatus", "Clinical Reference")
                                    .ifBlank { "Clinical Reference" }
                            )
                        )
                    }
                }
            }
            if (citationsList.isEmpty()) {
                citationsList.add(
                    NursingCitation(
                        title = "WHO / Potter & Perry / Brunner & Suddarth Nursing Reference",
                        url = "https://www.who.int",
                        publishedDate = "2025",
                        verificationStatus = "Evidence-Based Nursing Reference"
                    )
                )
            }

            val relatedList = mutableListOf<String>()
            val relatedArray = json.optJSONArray("relatedTopics")
            if (relatedArray != null) {
                for (i in 0 until relatedArray.length()) {
                    val item = relatedArray.optString(i).trim()
                    if (item.isNotEmpty()) relatedList.add(item)
                }
            }

            val emergencyWarning = json.optString("emergencyWarning", "").trim()

            return NursingAiResponse(
                summary = summary,
                sections = sectionsList,
                citations = citationsList,
                relatedTopics = relatedList.take(4),
                emergencyWarning = emergencyWarning,
                firebaseNotice = bi(
                    language,
                    "Live Gemini AI ($MODEL_NAME) • Educational Clinical Reference",
                    "লাইভ Gemini AI ($MODEL_NAME) • শিক্ষামূলক ক্লিনিক্যাল রেফারেন্স"
                ),
                isLiveGemini = true
            )
        }
    }
}

private object NursingSectionSerializer {
    fun parseSections(serialized: String): List<NursingSection> {
        if (serialized.isBlank()) return emptyList()
        return serialized.split("||SECTION||").mapNotNull { chunk ->
            val idx = chunk.indexOf("::")
            if (idx > 0) NursingSection(chunk.substring(0, idx), chunk.substring(idx + 2)) else null
        }
    }
}

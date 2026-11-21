package com.example.data.ai

import android.net.Uri
import com.example.data.local.AppLanguage
import com.example.data.local.db.ChatMessageEntity

data class NursingSection(
    val title: String,
    val body: String
)

data class NursingCitation(
    val title: String,
    val url: String,
    val publishedDate: String,
    val verificationStatus: String
)

data class NursingAiResponse(
    val summary: String,
    val sections: List<NursingSection>,
    val citations: List<NursingCitation>,
    val relatedTopics: List<String>,
    val emergencyWarning: String = "",
    val firebaseNotice: String,
    val isLiveGemini: Boolean = false
)

enum class SmartAiToolType(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val samplePromptEn: String,
    val samplePromptBn: String
) {
    TUTOR(
        id = "tutor",
        titleEn = "AI Nursing Tutor",
        titleBn = "AI নার্সিং টিউটর",
        subtitleEn = "Guided clinical reasoning & concept breakdowns",
        subtitleBn = "ধাপে ধাপে ক্লিনিক্যাল ব্যাখ্যা ও ধারণা",
        samplePromptEn = "Explain preload, afterload, and nursing care in heart failure",
        samplePromptBn = "হার্ট ফেইলিউরে প্রিলোড, আফটারলোড ও নার্সিং কেয়ার বুঝিয়ে বলো"
    ),
    CASE_EXPLAINER(
        id = "case_explainer",
        titleEn = "AI Case Study Explainer",
        titleBn = "AI কেস স্টাডি বিশ্লেষক",
        subtitleEn = "Analyze patient cues, priorities & nursing actions",
        subtitleBn = "রোগীর লক্ষণ, অগ্রাধিকার ও নার্সিং পদক্ষেপ বিশ্লেষণ",
        samplePromptEn = "65yo COPD patient with SpO2 85%, RR 28, drowsy on 6L O2 mask",
        samplePromptBn = "৬৫ বছর বয়সী COPD রোগী, SpO2 ৮৫%, শ্বাস ২৮/মি এবং তন্দ্রাচ্ছন্ন"
    ),
    TRANSLATOR(
        id = "translator",
        titleEn = "Medical Term Translator (EN ↔ বাংলা)",
        titleBn = "মেডিকেল পরিভাষা অনুবাদক (EN ↔ বাংলা)",
        subtitleEn = "Accurate bilingual clinical & patient-friendly terms",
        subtitleBn = "ইংরেজি ও বাংলায় সঠিক মেডিকেল পরিভাষা অনুবাদ",
        samplePromptEn = "Translate & explain: Dyspnea, Cyanosis, Orthostatic Hypotension, Sepsis",
        samplePromptBn = "অনুবাদ ও ব্যাখ্যা করো: Hypertension, Hypoglycemia, Anaphylaxis, Asepsis"
    ),
    SUMMARIZER(
        id = "summarizer",
        titleEn = "AI Note Summarizer",
        titleBn = "AI নোট সারসংক্ষেপ",
        subtitleEn = "Turn long textbook notes into crisp exam bullets",
        subtitleBn = "দীর্ঘ নোট থেকে পরীক্ষার উপযোগী মূল পয়েন্ট তৈরি",
        samplePromptEn = "Summarize DKA pathophysiology, labs, and priority nursing interventions",
        samplePromptBn = "DKA-এর প্যাথোফিজিওলজি, ল্যাব ও জরুরি নার্সিং কেয়ার সংক্ষেপে দাও"
    ),
    MCQ_GEN(
        id = "mcq_gen",
        titleEn = "AI MCQ Generator",
        titleBn = "AI MCQ প্রশ্ন তৈরি",
        subtitleEn = "Generate NCLEX/BNMC-style MCQs with rationales",
        subtitleBn = "ব্যাখ্যাসহ NCLEX ও BNMC মানের MCQ অনুশীলন",
        samplePromptEn = "Generate 3 priority MCQs on Maternal PPH & Eclampsia",
        samplePromptBn = "প্রসব-পরবর্তী রক্তক্ষরণ ও এক্লাম্পসিয়ার উপর ৩টি MCQ তৈরি করো"
    ),
    VIVA_PRACTICE(
        id = "viva",
        titleEn = "AI Viva Practice",
        titleBn = "AI ভাইভা অনুশীলন",
        subtitleEn = "Simulated examiner Q&A with model answers",
        subtitleBn = "পরীক্ষকের সম্ভাব্য প্রশ্ন ও আদর্শ উত্তর অনুশীলন",
        samplePromptEn = "Ask me viva questions on Safe Medication Administration & IV Cannulation",
        samplePromptBn = "নিরাপদ ওষুধ প্রয়োগ ও IV ক্যানুলেশন বিষয়ে ভাইভা প্রশ্ন করো"
    ),
    ASSIGNMENT(
        id = "assignment",
        titleEn = "Assignment & Study Guide Assistant",
        titleBn = "অ্যাসাইনমেন্ট ও স্টাডি গাইড সহায়ক",
        subtitleEn = "Structured academic outlines & EBP frameworks",
        subtitleBn = "অ্যাসাইনমেন্ট কাঠামো ও EBP রেফারেন্স গাইড",
        samplePromptEn = "Create an outline for a Community Health Home Visit Assignment",
        samplePromptBn = "কমিউনিটি হেলথ হোম ভিজিট অ্যাসাইনমেন্টের রূপরেখা তৈরি করো"
    ),
    SCENARIO_PRACTICE(
        id = "scenario",
        titleEn = "Clinical Scenario Practice",
        titleBn = "ক্লিনিক্যাল সিনারিও অনুশীলন",
        subtitleEn = "Interactive ward simulation & ABCDE prioritization",
        subtitleBn = "জরুরি ওয়ার্ড সিমুলেশন ও ABCDE অগ্রাধিকার",
        samplePromptEn = "Post-op patient suddenly develops chest pain and SpO2 88%",
        samplePromptBn = "অপারেশনের পর রোগীর হঠাৎ বুকে ব্যথা ও SpO2 ৮৮% হলে করণীয়"
    )
}

interface NursingAiRepository {
    suspend fun askNursingQuestion(
        query: String,
        history: List<ChatMessageEntity>,
        language: AppLanguage,
        attachedImageLabel: String? = null,
        attachedImageUri: Uri? = null,
        onStreamingUpdate: ((String) -> Unit)? = null
    ): NursingAiResponse

    suspend fun generateCarePlan(
        patientCase: String,
        language: AppLanguage
    ): NursingAiResponse

    suspend fun runSmartTool(
        toolType: SmartAiToolType,
        input: String,
        language: AppLanguage
    ): NursingAiResponse
}

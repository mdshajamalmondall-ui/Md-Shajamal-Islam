package com.example.data.ai

import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.data.local.db.ChatMessageEntity
import com.example.data.local.db.ClinicalGlossaryData
import com.example.data.local.db.DrugEntity
import com.example.data.local.db.GlossaryTerm
import com.example.data.local.db.KnowledgeDocEntity
import com.example.data.local.db.NursingDatabase
import com.example.data.local.db.ProcedureEntity
import com.example.data.local.db.StudyItemEntity
import kotlinx.coroutines.delay

class LocalGroundedAiRepository(
    private val db: NursingDatabase
) : NursingAiRepository {

    private fun noticeFor(language: AppLanguage): String = bi(
        language,
        en = "Sample AI Notice: Live Gemini AI requires Firebase AI Logic. Showing grounded educational reference & clearly labeled sample synthesis.",
        bn = "নমুনা AI বিজ্ঞপ্তি: লাইভ Gemini AI ব্যবহারের জন্য Firebase প্রয়োজন। বর্তমানে যাচাইকৃত স্থানীয় রেফারেন্স ও নমুনা উত্তর প্রদর্শিত হচ্ছে।"
    )

    override suspend fun askNursingQuestion(
        query: String,
        history: List<ChatMessageEntity>,
        language: AppLanguage,
        attachedImageLabel: String?,
        attachedImageUri: android.net.Uri?,
        onStreamingUpdate: ((String) -> Unit)?
    ): NursingAiResponse {
        delay(280)

        val effectiveLanguage = detectEffectiveLanguage(query, language)
        val combinedQuery = buildContextAwareQuery(query, history, attachedImageLabel)
        val meaningfulTokens = ClinicalGlossaryData.extractMeaningfulTokens(combinedQuery)
        val nonGenericTokens = meaningfulTokens.filter { it !in genericNursingWords }

        val docs = db.knowledgeDocDao().getAllOnce()
        val drugs = db.drugDao().getAllOnce()
        val procedures = db.procedureDao().getAllOnce()
        val studyItems = db.studyItemDao().getAllOnce()

        val bestDoc = scoreDocs(docs, combinedQuery, meaningfulTokens, nonGenericTokens)
        val bestDrug = scoreDrugs(drugs, combinedQuery, nonGenericTokens)
        val bestProc = scoreProcedures(procedures, combinedQuery, nonGenericTokens)
        val bestGlossary = ClinicalGlossaryData.findBestMatch(combinedQuery, meaningfulTokens)
        val bestStudy = scoreStudyItems(studyItems, combinedQuery, nonGenericTokens)

        val emergencyWarning = detectEmergency(combinedQuery, bestDoc?.first, effectiveLanguage)

        if (attachedImageLabel != null) {
            return buildImageGroundedResponse(
                query = query,
                imageLabel = attachedImageLabel,
                bestDoc = bestDoc?.first,
                bestDrug = bestDrug?.first,
                bestProc = bestProc?.first,
                language = effectiveLanguage,
                emergencyWarning = emergencyWarning
            )
        }

        val docScore = bestDoc?.second ?: 0
        val drugScore = bestDrug?.second ?: 0
        val procScore = bestProc?.second ?: 0
        val glossaryScore = bestGlossary?.second ?: 0
        val studyScore = bestStudy?.second ?: 0

        val isDefQuery = ClinicalGlossaryData.isDefinitionIntent(query)

        // 1. If user asked a crisp definition of a glossary term (e.g. "definition of cyanosis", "what is tachycardia")
        // and no broader full-disease KnowledgeDoc outscores it decisively
        if (bestGlossary != null && isDefQuery && glossaryScore >= docScore && glossaryScore >= drugScore) {
            return formatGlossaryResponse(bestGlossary.first, effectiveLanguage, emergencyWarning)
        }

        // 2. Strong Drug match
        if (bestDrug != null && drugScore >= docScore && drugScore >= procScore && drugScore >= glossaryScore) {
            return formatDrugResponse(bestDrug.first, effectiveLanguage, emergencyWarning)
        }

        // 3. Strong Procedure match
        if (bestProc != null && procScore >= docScore && procScore >= glossaryScore) {
            return formatProcedureResponse(bestProc.first, effectiveLanguage, emergencyWarning)
        }

        // 4. Strong KnowledgeDoc match (including Definition of Nursing, Pain, Fever, Shock, MI, Stroke, etc.)
        if (bestDoc != null && docScore >= glossaryScore) {
            return formatKnowledgeDocResponse(bestDoc.first, query, effectiveLanguage, emergencyWarning)
        }

        // 5. Glossary term match for non-definition queries (e.g. searching "triage" or "asepsis")
        if (bestGlossary != null) {
            return formatGlossaryResponse(bestGlossary.first, effectiveLanguage, emergencyWarning)
        }

        // 6. Study Item match (notes, viva, short Q&A, NANDA care plan)
        if (bestStudy != null) {
            return formatStudyItemResponse(bestStudy.first, effectiveLanguage, emergencyWarning)
        }

        // 7. Honest topic-aware educational guidance if no strong local topic matched
        return buildGeneralEducationalFallback(query, effectiveLanguage, docs.take(5), emergencyWarning)
    }

    override suspend fun generateCarePlan(
        patientCase: String,
        language: AppLanguage
    ): NursingAiResponse {
        delay(350)
        val lang = detectEffectiveLanguage(patientCase, language)
        val tokens = ClinicalGlossaryData.extractMeaningfulTokens(patientCase)
        val nonGeneric = tokens.filter { it !in genericNursingWords }
        val matchedDoc = scoreDocs(db.knowledgeDocDao().getAllOnce(), patientCase, tokens, nonGeneric)?.first

        val isResp = tokens.any { it in setOf("dyspnea", "spo2", "copd", "asthma", "pneumonia", "cough", "breathing", "শ্বাসকষ্ট", "নিউমোনিয়া") }
        val isCardiac = tokens.any { it in setOf("bp", "hypertension", "chest", "cardiac", "heart", "edema", "mi", "রক্তচাপ", "হার্ট") }
        val isMaternal = tokens.any { it in setOf("pregnant", "pph", "bleeding", "postpartum", "eclampsia", "seizure", "প্রসব", "এক্লাম্পসিয়া") }
        val isPain = tokens.any { it in setOf("pain", "ache", "fracture", "postoperative", "ব্যথা") }

        val tuple = when {
            isMaternal -> CarePlanTuple(
                "1. Deficient Fluid Volume / Risk for Maternal Shock (NANDA-I)\n2. Risk for Injury related to eclamptic seizures",
                "১. রক্তক্ষরণজনিত ফ্লুইড ভলিউম ঘাটতি (NANDA-I)\n২. এক্লাম্পটিক খিঁচুনিজনিত আঘাতের ঝুঁকি",
                "• NOC: Maintain SBP >= 90 mmHg, firm uterine fundus, and urine output >= 30 mL/hr within 1 hour.",
                "• NOC: ১ ঘণ্টার মধ্যে রক্তচাপ স্থিতিশীল রাখা, জরায়ু শক্ত রাখা ও প্রস্রাব >= ৩০ mL/hr নিশ্চিত করা।",
                "• Perform firm fundal massage & quantify blood loss (QBL).\n• Establish two 16–18G IV lines and give ordered Oxytocin/MgSO4.\n• Monitor DTRs, RR (>=12/min), and keep Calcium Gluconate at bedside.",
                "• ফান্ডাল ম্যাসাজ ও রক্তক্ষরণ পরিমাপ করুন।\n• দুটি ১৬–১৮G IV ক্যানুলা স্থাপন করে অক্সিটোসিন/MgSO4 দিন।\n• রিফ্লেক্স ও ক্যালসিয়াম গ্লুকোনেট হাতের কাছে রাখুন।"
            )
            isResp -> CarePlanTuple(
                "1. Impaired Gas Exchange (NANDA-I) related to ventilation-perfusion mismatch\n2. Ineffective Airway Clearance related to secretions",
                "১. গ্যাস বিনিময়ে ব্যাঘাত (NANDA-I)\n২. কফের কারণে শ্বাসনালী পরিষ্কারে অক্ষমতা",
                "• NOC: Maintain SpO2 >= 94% (88–92% if COPD) and RR 12–20/min within 2–4 hours.",
                "• NOC: ২–৪ ঘণ্টার মধ্যে SpO2 >= ৯৪% (COPD-তে ৮৮–৯২%) এবং স্বাভাবিক শ্বাসের হার বজায় রাখা।",
                "• Position in Semi-Fowler's (30–45°).\n• Administer titrated O2 & ordered bronchodilators.\n• Encourage deep breathing, coughing, and hydration.",
                "• সেমি-ফাউলার পজিশনে (৩০–৪৫°) রাখুন।\n• নির্ধারিত মাত্রায় অক্সিজেন ও ব্রঙ্কোডাইলেটর দিন।"
            )
            isCardiac -> CarePlanTuple(
                "1. Decreased Cardiac Output / Risk for Ineffective Tissue Perfusion (NANDA-I)\n2. Activity Intolerance",
                "১. কার্ডিয়াক আউটপুট হ্রাস / টিস্যু পারফিউশনের ঝুঁকি (NANDA-I)\n২. শারীরিক পরিশ্রমে অসহিষ্ণুতা",
                "• NOC: Stabilize BP within target range, absence of chest pain, urine output >= 30 mL/hr.",
                "• NOC: রক্তচাপ নিয়ন্ত্রণে রাখা, বুকে ব্যথা না থাকা এবং ফুসফুস পরিষ্কার থাকা।",
                "• Monitor vitals, 12-lead ECG, and daily weights.\n• Give ordered antihypertensives/diuretics after checking BP and K+.\n• Teach DASH low-sodium diet (<2g/day).",
                "• নিয়মিত রক্তচাপ, ইসিজি ও ওজন পর্যবেক্ষণ করুন।\n• BP ও পটাশিয়াম দেখে ওষুধ প্রয়োগ করুন।"
            )
            isPain -> CarePlanTuple(
                "1. Acute Pain (NANDA-I) related to tissue trauma/inflammation\n2. Impaired Physical Mobility secondary to pain",
                "১. তীব্র ব্যথা (Acute Pain — NANDA-I)\n২. ব্যথার কারণে নড়াচড়ায় অসুবিধা",
                "• NOC: Patient reports pain <= 3/10 within 45–60 minutes of intervention and rests comfortably.",
                "• NOC: ৪৫–৬০ মিনিটের মধ্যে ব্যথা <= ৩/১০-এ নামিয়ে আনা এবং আরাম নিশ্চিত করা।",
                "• Assess PQRST pain score before & 45 min after analgesics.\n• Administer scheduled WHO-ladder analgesia + non-pharmacologic positioning/heat-cold.\n• Monitor RR (>=12/min) if opioids given.",
                "• ওষুধ দেওয়ার আগে ও ৪৫ মিনিট পরে PQRST স্কোর দেখুন।\n• নির্ধারিত ব্যথানাশক ও আরামদায়ক পজিশনিং দিন।"
            )
            else -> CarePlanTuple(
                "1. Acute Knowledge Deficit / Self-Health Management Readiness (NANDA-I)\n2. Risk for Clinical Deterioration",
                "১. স্বাস্থ্য ব্যবস্থাপনা সম্পর্কিত জ্ঞানের ঘাটতি (NANDA-I)\n২. রোগের জটিলতার ঝুঁকি",
                "• NOC: Patient/caregiver verbalizes 3 red-flag symptoms and demonstrates care plan adherence prior to discharge.",
                "• NOC: রোগী ও পরিবার ৩টি বিপদচিহ্ন চিনতে পারবে এবং সঠিক যত্ন পদ্ধতি বলতে পারবে।",
                "• Complete focused head-to-toe & vital signs assessment.\n• Implement evidence-based NIC interventions.\n• Provide teach-back bilingual education.",
                "• সম্পূর্ণ হেড-টু-টো ও ভাইটাল সাইন মূল্যায়ন করুন।\n• প্রমাণ-ভিত্তিক NIC নার্সিং হস্তক্ষেপ প্রয়োগ করুন।"
            )
        }

        val sections = listOf(
            NursingSection(
                bi(lang, "1. Patient Assessment", "১. রোগীর মূল্যায়ন (Assessment)"),
                bi(lang, "Case Input: $patientCase\nFocus: Vital signs, focused exam, labs & allergies.", "রোগীর তথ্য: $patientCase\nমূল্যায়ন: ভাইটাল সাইন, সিস্টেম-ভিত্তিক পরীক্ষা ও ল্যাব রিপোর্ট।")
            ),
            NursingSection(bi(lang, "2. Nursing Diagnosis (NANDA-I)", "২. নার্সিং ডায়াগনোসিস (NANDA-I)"), bi(lang, tuple.diagEn, tuple.diagBn)),
            NursingSection(bi(lang, "3. Goals & Expected Outcomes (NOC)", "৩. লক্ষ্য ও প্রত্যাশিত ফলাফল (NOC)"), bi(lang, tuple.goalsEn, tuple.goalsBn)),
            NursingSection(bi(lang, "4. Nursing Interventions (NIC)", "৪. নার্সিং হস্তক্ষেপ (NIC)"), bi(lang, tuple.intervEn, tuple.intervBn)),
            NursingSection(
                bi(lang, "5. Clinical Rationales", "৫. বৈজ্ঞানিক যুক্তি (Rationales)"),
                matchedDoc?.let { bi(lang, it.interventionsRationaleEn, it.interventionsRationaleBn) }
                    ?: bi(lang, "• ABCDE prioritization stabilizes life-threatening issues first.\n• Frequent vital trends catch deterioration early.", "• ABCDE অগ্রাধিকার জীবন-হুমকিপূর্ণ সমস্যা আগে স্থিতিশীল করে।")
            ),
            NursingSection(
                bi(lang, "6. Evaluation", "৬. মূল্যায়ন (Evaluation)"),
                bi(lang, "Reassess vital signs and NOC indicators at scheduled intervals; document met/unmet goals.", "নির্ধারিত সময় পরপর ভাইটাল সাইন ও লক্ষণ পুনরায় মূল্যায়ন করুন।")
            )
        )

        val citations = listOfNotNull(
            matchedDoc?.let { NursingCitation(it.sourceTitle, it.sourceUrl, it.publishedDate, it.verificationStatus) },
            NursingCitation("NANDA-I, NIC & NOC Educational Nursing Taxonomy", "https://nanda.org", "2024-12-01", "Educational Reference")
        )

        return NursingAiResponse(
            summary = bi(lang, "Educational Nursing Care Plan (Sample Template)", "শিক্ষামূলক নার্সিং কেয়ার প্ল্যান (নমুনা টেমপ্লেট)"),
            sections = sections,
            citations = citations,
            relatedTopics = listOf("NANDA-I Nursing Diagnoses", "10 Rights of Medication", "SBAR Handover"),
            emergencyWarning = detectEmergency(patientCase, matchedDoc, lang),
            firebaseNotice = noticeFor(lang)
        )
    }

    override suspend fun runSmartTool(
        toolType: SmartAiToolType,
        input: String,
        language: AppLanguage
    ): NursingAiResponse {
        delay(300)
        val lang = detectEffectiveLanguage(input, language)
        val baseRag = askNursingQuestion(input, emptyList(), lang)
        val toolSectionTitle = bi(lang, "Smart Tool Output — ${toolType.titleEn}", "স্মার্ট টুল ফলাফল — ${toolType.titleBn}")

        val customBody = when (toolType) {
            SmartAiToolType.TRANSLATOR -> bi(
                lang,
                "Bilingual Clinical Translation Guide:\n• Dyspnea = শ্বাসকষ্ট\n• Hypertension = উচ্চ রক্তচাপ\n• Hypoglycemia = রক্তে শর্করা <70 mg/dL\n• Cyanosis = ত্বক/ঠোঁট নীলচে হওয়া\n• Asepsis = জীবাণুমুক্ত অবস্থা\n• Edema = শরীরে পানি জমা (শোথ)",
                "দ্বিভাষিক ক্লিনিক্যাল অনুবাদ গাইড:\n• Dyspnea = শ্বাসকষ্ট\n• Hypertension = উচ্চ রক্তচাপ\n• Hypoglycemia = রক্তে গ্লুকোজ <৭০ mg/dL\n• Cyanosis = নীলচে ভাব\n• Asepsis = জীবাণুমুক্ত পদ্ধতি"
            )
            SmartAiToolType.MCQ_GEN -> bi(
                lang,
                "Sample Exam MCQ (${input.take(40)}):\nQ. First priority when a patient has acute respiratory distress (SpO2 87%)?\nA) Chest X-ray first\nB) High-Fowler's position & ordered O2 ✓\nC) Sputum culture\nD) Document only\nRationale: Airway & Breathing (ABCs) precede diagnostic tasks.",
                "নমুনা MCQ প্রশ্ন:\nপ্রশ্ন: তীব্র শ্বাসকষ্ট (SpO2 ৮৭%) হলে নার্সের প্রথম কাজ কী?\nA) আগে এক্স-রে\nB) হাই-ফাউলার পজিশন ও অক্সিজেন দেওয়া ✓\nC) কফ সংগ্রহ\nD) শুধু নোট লেখা\nব্যাখ্যা: ABC নীতি অনুযায়ী শ্বাসনালী ও অক্সিজেন সর্বদা প্রথম।"
            )
            SmartAiToolType.VIVA_PRACTICE -> bi(
                lang,
                "Examiner Viva Q&A:\n• Examiner: 'State the 10 Rights of Medication Administration.'\n• Answer: Right Patient, Drug, Dose, Route, Time, Reason, Assessment, Documentation, Evaluation, Right to Refuse.\n• Follow-up: 'Antidote for MgSO4 toxicity?' -> 10% Calcium Gluconate 1 g IV slowly.",
                "ভাইভা অনুশীলন:\n• পরীক্ষক: 'ওষুধ প্রয়োগের ১০ অধিকার বলুন।'\n• উত্তর: সঠিক রোগী, ওষুধ, ডোজ, পথ, সময়, কারণ, মূল্যায়ন, নথি, ফলাফল ও প্রত্যাখ্যান।\n• MgSO4 অ্যান্টিডোট -> ১০% ক্যালসিয়াম গ্লুকোনেট।"
            )
            else -> baseRag.sections.take(3).joinToString("\n\n") { "${it.title}:\n${it.body}" }
        }

        return baseRag.copy(
            summary = bi(lang, "${toolType.titleEn} (Sample & Grounded Mode)", "${toolType.titleBn} (নমুনা ও যাচাইকৃত মোড)"),
            sections = listOf(NursingSection(toolSectionTitle, customBody)) + baseRag.sections.take(3),
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun buildContextAwareQuery(query: String, history: List<ChatMessageEntity>, attachedImageLabel: String?): String {
        val followUpHints = listOf(
            "complication", "rationale", "prevention", "dose", "dosage", "treatment",
            "cause", "symptom", "more", "its", "this", "জটিলতা", "করণীয়", "ওষুধ", "লক্ষণ", "প্রতিরোধ"
        )
        val isShortFollowUp = query.split(" ").size <= 5 &&
            !ClinicalGlossaryData.isDefinitionIntent(query) &&
            followUpHints.any { query.lowercase().contains(it) }
        val prevUserMsg = history.lastOrNull { it.role == "user" }?.content.orEmpty()
        return listOfNotNull(
            if (isShortFollowUp && prevUserMsg.isNotBlank()) prevUserMsg else null,
            query,
            attachedImageLabel
        ).joinToString(" ")
    }

    private fun detectEffectiveLanguage(query: String, pref: AppLanguage): AppLanguage {
        return if (query.any { it in '\u0980'..'\u09FF' }) AppLanguage.BENGALI else pref
    }

    private fun scoreDocs(
        docs: List<KnowledgeDocEntity>,
        rawQuery: String,
        allTokens: List<String>,
        nonGenericTokens: List<String>
    ): Pair<KnowledgeDocEntity, Int>? {
        val q = rawQuery.lowercase().trim()
        val isNursingDef = q.contains("definition of nursing") || q.contains("what is nursing") ||
            q.contains("define nursing") || q.contains("নার্সিং কাকে বলে") || q.contains("নার্সিং সংজ্ঞা") || q == "nursing"
        val isPainQuery = q.contains("pain") || q.contains("ব্যথা") || q.contains("পেইন")

        val tokensToUse = if (isNursingDef) allTokens else nonGenericTokens.ifEmpty { allTokens }

        return docs.map { doc ->
            var score = 0
            if (isNursingDef && doc.id == "kb_definition_of_nursing") score += 60
            if (isPainQuery && doc.id == "kb_pain_management") score += 50

            val titleWords = "${doc.titleEn} ${doc.titleBn}".lowercase().split(Regex("[^a-z0-9\\u0980-\\u09FF]+")).toSet()
            val keywordWords = doc.keywords.lowercase().split(Regex("[^a-z0-9\\u0980-\\u09FF]+")).toSet()
            val subjectWords = doc.subject.lowercase().split(Regex("[^a-z0-9\\u0980-\\u09FF]+")).toSet()
            val defLower = "${doc.definitionEn} ${doc.definitionBn}".lowercase()

            for (t in tokensToUse) {
                if (t in titleWords) score += 12
                else if (doc.titleBn.lowercase().contains(t) && t.length >= 3) score += 10

                if (t in keywordWords) score += 9
                else if (doc.keywords.lowercase().contains(t) && t.length >= 4) score += 6

                if (t in subjectWords && t != "nursing") score += 4
                if (Regex("\\b${Regex.escape(t)}\\b").containsMatchIn(defLower)) score += 3
            }
            doc to score
        }.filter { it.second >= 8 }.maxByOrNull { it.second }
    }

    private fun scoreDrugs(drugs: List<DrugEntity>, rawQuery: String, tokens: List<String>): Pair<DrugEntity, Int>? {
        val q = rawQuery.lowercase()
        return drugs.map { drug ->
            var score = 0
            val genWords = drug.genericName.lowercase().split(Regex("[^a-z0-9]+")).toSet()
            val brandWords = drug.brandNames.lowercase().split(Regex("[^a-z0-9]+")).toSet()
            val classLower = "${drug.classification} ${drug.indications}".lowercase()
            if (q.contains(drug.genericName.lowercase())) score += 25
            for (t in tokens) {
                if (t in genWords) score += 16
                if (t in brandWords) score += 12
                if (Regex("\\b${Regex.escape(t)}\\b").containsMatchIn(classLower)) score += 3
            }
            drug to score
        }.filter { it.second >= 10 }.maxByOrNull { it.second }
    }

    private fun scoreProcedures(procs: List<ProcedureEntity>, rawQuery: String, tokens: List<String>): Pair<ProcedureEntity, Int>? {
        val q = rawQuery.lowercase()
        return procs.map { proc ->
            var score = 0
            val titleWords = "${proc.titleEn} ${proc.titleBn}".lowercase().split(Regex("[^a-z0-9\\u0980-\\u09FF]+")).toSet()
            val defLower = "${proc.category} ${proc.definition}".lowercase()
            if (q.contains(proc.titleEn.lowercase())) score += 24
            for (t in tokens) {
                if (t in titleWords) score += 12
                if (Regex("\\b${Regex.escape(t)}\\b").containsMatchIn(defLower)) score += 3
            }
            proc to score
        }.filter { it.second >= 10 }.maxByOrNull { it.second }
    }

    private fun scoreStudyItems(items: List<StudyItemEntity>, rawQuery: String, tokens: List<String>): Pair<StudyItemEntity, Int>? {
        val q = rawQuery.lowercase()
        return items.map { item ->
            var score = 0
            val titleWords = "${item.titleEn} ${item.titleBn}".lowercase().split(Regex("[^a-z0-9\\u0980-\\u09FF]+")).toSet()
            if (q.contains(item.titleEn.lowercase())) score += 20
            for (t in tokens) {
                if (t in titleWords) score += 10
                if (Regex("\\b${Regex.escape(t)}\\b").containsMatchIn("${item.bodyEn} ${item.bodyBn}".lowercase())) score += 2
            }
            item to score
        }.filter { it.second >= 10 }.maxByOrNull { it.second }
    }

    private fun detectEmergency(query: String, doc: KnowledgeDocEntity?, lang: AppLanguage): String {
        val q = query.lowercase()
        val urgentTerms = listOf("chest pain", "unresponsive", "anaphylaxis", "seizure", "bleeding", "suicide", "arrest", "stridor", "shock", "খিঁচুনি", "রক্তপাত", "বুকে ব্যথা")
        if (urgentTerms.any { q.contains(it) }) {
            return doc?.emergencyFlag?.takeIf { it.isNotBlank() } ?: bi(
                lang,
                "Clinical Safety Alert: Symptoms mentioned may indicate a medical emergency. Follow institutional rapid-response protocols.",
                "জরুরি সতর্কতা: উল্লিখিত লক্ষণগুলো জীবন-হুমকিপূর্ণ হতে পারে। অবিলম্বে জরুরি চিকিৎসা সেবা বা ইমার্জেন্সি প্রোটোকল অনুসরণ করুন।"
            )
        }
        return doc?.emergencyFlag.orEmpty()
    }

    private fun formatKnowledgeDocResponse(doc: KnowledgeDocEntity, query: String, lang: AppLanguage, emergencyWarning: String): NursingAiResponse {
        val q = query.lowercase()
        val allSections = listOf(
            NursingSection(bi(lang, "1. Definition", "১. সংজ্ঞা (Definition)"), bi(lang, doc.definitionEn, doc.definitionBn)),
            NursingSection(bi(lang, "2. Introduction", "২. ভূমিকা (Introduction)"), bi(lang, doc.introductionEn, doc.introductionBn)),
            NursingSection(bi(lang, "3. Causes / Risk Factors", "৩. কারণ ও ঝুঁকির উপাদান"), bi(lang, doc.causesRiskEn, doc.causesRiskBn)),
            NursingSection(bi(lang, "4. Signs and Symptoms", "৪. লক্ষণ ও উপসর্গ"), bi(lang, doc.signsSymptomsEn, doc.signsSymptomsBn)),
            NursingSection(bi(lang, "5. Pathophysiology", "৫. প্যাথোফিজিওলজি"), bi(lang, doc.pathophysiologyEn, doc.pathophysiologyBn)),
            NursingSection(bi(lang, "6. Assessment", "৬. নার্সিং মূল্যায়ন (Assessment)"), bi(lang, doc.assessmentEn, doc.assessmentBn)),
            NursingSection(bi(lang, "7. Investigations", "৭. পরীক্ষা-নিরীক্ষা (Investigations)"), bi(lang, doc.investigationsEn, doc.investigationsBn)),
            NursingSection(bi(lang, "8. Medical Management", "৮. মেডিকেল ব্যবস্থাপনা"), bi(lang, doc.medicalManagementEn, doc.medicalManagementBn)),
            NursingSection(bi(lang, "9. Nursing Management", "৯. নার্সিং ব্যবস্থাপনা"), bi(lang, doc.nursingManagementEn, doc.nursingManagementBn)),
            NursingSection(bi(lang, "10. Nursing Interventions & Rationale", "১০. নার্সিং হস্তক্ষেপ ও যুক্তি"), bi(lang, doc.interventionsRationaleEn, doc.interventionsRationaleBn)),
            NursingSection(bi(lang, "11. Complications", "১১. জটিলতা (Complications)"), bi(lang, doc.complicationsEn, doc.complicationsBn)),
            NursingSection(bi(lang, "12. Prevention", "১২. প্রতিরোধ (Prevention)"), bi(lang, doc.preventionEn, doc.preventionBn)),
            NursingSection(bi(lang, "13. Patient Education", "১৩. রোগী ও পরিবারের শিক্ষা"), bi(lang, doc.patientEducationEn, doc.patientEducationBn)),
            NursingSection(bi(lang, "14. Important Nursing Notes", "১৪. গুরুত্বপূর্ণ নার্সিং নোট"), bi(lang, doc.nursingNotesEn, doc.nursingNotesBn))
        )

        val isFocusedDef = ClinicalGlossaryData.isDefinitionIntent(q) &&
            !q.contains("management") && !q.contains("symptom") && !q.contains("cause")
        val focused = when {
            isFocusedDef -> allSections.filterIndexed { i, _ -> i in listOf(0, 1, 5, 8, 13) }
            q.contains("complication") || q.contains("জটিলতা") -> allSections.filterIndexed { i, _ -> i in listOf(0, 10, 13) }
            q.contains("symptom") || q.contains("sign") || q.contains("লক্ষণ") -> allSections.filterIndexed { i, _ -> i in listOf(0, 3, 5, 13) }
            q.contains("cause") || q.contains("risk") || q.contains("কারণ") -> allSections.filterIndexed { i, _ -> i in listOf(0, 2, 4, 11) }
            q.contains("management") || q.contains("intervention") || q.contains("ব্যবস্থাপনা") -> allSections.filterIndexed { i, _ -> i in listOf(0, 5, 8, 9, 12, 13) }
            else -> allSections
        }

        return NursingAiResponse(
            summary = bi(lang, doc.titleEn, doc.titleBn),
            sections = focused,
            citations = listOf(NursingCitation(doc.sourceTitle, doc.sourceUrl, doc.publishedDate, doc.verificationStatus)),
            relatedTopics = listOf(
                bi(lang, "${doc.titleEn.substringBefore(" —")} Management", "${doc.titleBn.substringBefore(" —")} ব্যবস্থাপনা"),
                bi(lang, "Complications & Prevention", "জটিলতা ও প্রতিরোধ"),
                bi(lang, "Nursing Care Plan", "নার্সিং কেয়ার প্ল্যান")
            ),
            emergencyWarning = emergencyWarning,
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun formatGlossaryResponse(term: GlossaryTerm, lang: AppLanguage, emergencyWarning: String): NursingAiResponse {
        val sections = listOf(
            NursingSection(bi(lang, "1. Definition", "১. সংজ্ঞা (Definition)"), bi(lang, term.definitionEn, term.definitionBn)),
            NursingSection(bi(lang, "2. Clinical Significance", "২. ক্লিনিক্যাল গুরুত্ব"), bi(lang, term.clinicalSignificanceEn, term.clinicalSignificanceBn)),
            NursingSection(bi(lang, "3. Priority Nursing Action", "৩. নার্সিং করণীয়"), bi(lang, term.nursingActionEn, term.nursingActionBn))
        )
        return NursingAiResponse(
            summary = ClinicalGlossaryData.formatTermTitle(term, lang),
            sections = sections,
            citations = listOf(
                NursingCitation("Potter & Perry's Fundamentals of Nursing / WHO & ICN Clinical Terminology", "https://www.who.int", "2025-01-10", "Verified Definition")
            ),
            relatedTopics = listOf("Definition of Nursing", "Definition of Pain", "Vital Signs Normal Range", "Asepsis"),
            emergencyWarning = emergencyWarning,
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun formatDrugResponse(drug: DrugEntity, lang: AppLanguage, emergencyWarning: String): NursingAiResponse {
        val sections = listOf(
            NursingSection(bi(lang, "1. Drug Overview & Classification", "১. ওষুধের পরিচিতি ও শ্রেণিবিভাগ"), "${drug.genericName} (${drug.brandNames})\nClass: ${drug.classification}"),
            NursingSection(bi(lang, "2. Indications & Mechanism of Action", "২. ব্যবহার ও কার্যপদ্ধতি"), "Indications: ${drug.indications}\nMOA: ${drug.mechanismOfAction}"),
            NursingSection(bi(lang, "3. Dosage Reference & Route", "৩. ডোজ ও প্রয়োগের পথ"), "Dosage: ${drug.dosageReference}\nRoute: ${drug.route}"),
            NursingSection(bi(lang, "4. Contraindications & Adverse Reactions", "৪. প্রতিনির্দেশনা ও পার্শ্বপ্রতিক্রিয়া"), "Contraindications: ${drug.contraindications}\nSide Effects: ${drug.sideEffects}\nSerious ADRs: ${drug.adverseReactions}"),
            NursingSection(bi(lang, "5. Nursing Responsibilities & Counseling", "৫. নার্সিং দায়িত্ব ও পরামর্শ"), "Nursing Care: ${drug.nursingResponsibilities}\nInteractions: ${drug.drugInteractions}\nCounseling: ${drug.patientCounseling}")
        )
        return NursingAiResponse(
            summary = "${drug.genericName} — ${drug.classification}",
            sections = sections,
            citations = listOf(NursingCitation(drug.sourceTitle, "https://www.who.int", drug.updatedDate, drug.verificationStatus)),
            relatedTopics = listOf("10 Rights of Medication", "Dosage Calculator", "IV Flow Rate"),
            emergencyWarning = if (drug.isHighAlert) {
                bi(lang, "HIGH-ALERT MEDICATION: Independent double-check required prior to administration.", "হাই-অ্যালার্ট ওষুধ: প্রয়োগের আগে ডোজ ও নাম দ্বিতীয়বার যাচাই করুন।")
            } else emergencyWarning,
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun formatProcedureResponse(proc: ProcedureEntity, lang: AppLanguage, emergencyWarning: String): NursingAiResponse {
        val sections = listOf(
            NursingSection(bi(lang, "1. Definition & Purpose", "১. সংজ্ঞা ও উদ্দেশ্য"), "${proc.definition}\n\nPurpose: ${proc.purpose}"),
            NursingSection(bi(lang, "2. Equipment & Preparation", "২. সরঞ্জাম ও প্রস্তুতি"), "Equipment: ${proc.equipment}\n\nPreparation: ${proc.preparation}"),
            NursingSection(bi(lang, "3. Step-by-Step Procedure", "৩. ধাপে ধাপে পদ্ধতি"), proc.steps),
            NursingSection(bi(lang, "4. Nursing Responsibilities & Infection Control", "৪. নার্সিং দায়িত্ব ও সংক্রমণ নিয়ন্ত্রণ"), "Responsibilities: ${proc.nursingResponsibilities}\n\nInfection Control: ${proc.infectionControl}"),
            NursingSection(bi(lang, "5. Complications & Documentation", "৫. জটিলতা ও নথিভুক্তকরণ"), "Complications: ${proc.complications}\n\nDocumentation: ${proc.documentation}")
        )
        return NursingAiResponse(
            summary = bi(lang, proc.titleEn, proc.titleBn),
            sections = sections,
            citations = listOf(NursingCitation(proc.sourceTitle, "https://www.who.int", proc.updatedDate, proc.verificationStatus)),
            relatedTopics = listOf("Asepsis", "Hand Washing", "Vital Signs"),
            emergencyWarning = if (proc.supervisedTrainingRequired) {
                bi(lang, "Clinical Supervision Notice: Perform invasive procedures only under supervised clinical training.", "প্রশিক্ষণ সতর্কতা: এই প্রসিডিউরটি তত্ত্বাবধানে প্রশিক্ষণের অধীনে সম্পন্ন করুন।")
            } else emergencyWarning,
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun formatStudyItemResponse(item: StudyItemEntity, lang: AppLanguage, emergencyWarning: String): NursingAiResponse {
        val sections = listOf(
            NursingSection(bi(lang, "1. ${item.subject} — Key Study Note", "১. ${item.subject} — স্টাডি নোট"), bi(lang, item.bodyEn, item.bodyBn))
        )
        return NursingAiResponse(
            summary = bi(lang, item.titleEn, item.titleBn),
            sections = sections,
            citations = listOf(NursingCitation(item.sourceTitle, item.sourceUrl.ifBlank { "https://www.who.int" }, item.updatedDate, item.verificationStatus)),
            relatedTopics = listOf("Definition of Nursing", "Definition of Pain", "Vital Signs Normal Range"),
            emergencyWarning = emergencyWarning,
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun buildImageGroundedResponse(
        query: String, imageLabel: String, bestDoc: KnowledgeDocEntity?, bestDrug: DrugEntity?, bestProc: ProcedureEntity?, language: AppLanguage, emergencyWarning: String
    ): NursingAiResponse {
        val matchedSummary = bestDoc?.let { bi(language, it.titleEn, it.titleBn) }
            ?: bestDrug?.genericName ?: bestProc?.let { bi(language, it.titleEn, it.titleBn) }
            ?: bi(language, "Uploaded Nursing Study Material", "আপলোডকৃত নার্সিং স্টাডি ছবি")
        val sections = listOf(
            NursingSection(
                bi(language, "1. Image Analysis Status (Sample / Local Mode)", "১. ছবি বিশ্লেষণ অবস্থা (নমুনা মোড)"),
                bi(language, "Attached image: $imageLabel\nNote: Live multimodal vision analysis requires Firebase AI Logic. Grounded response matched to your text query below.", "সংযুক্ত ছবি: $imageLabel\nবিঃদ্রঃ সরাসরি ছবির ভিশন বিশ্লেষণের জন্য Firebase AI Logic প্রয়োজন।")
            ),
            NursingSection(
                bi(language, "2. Structured Breakdown ($matchedSummary)", "২. সম্পর্কিত নার্সিং ব্যাখ্যা ($matchedSummary)"),
                bestDoc?.let { bi(language, "${it.definitionEn}\n\nNursing Management:\n${it.nursingManagementEn}", "${it.definitionBn}\n\nনার্সিং ব্যবস্থাপনা:\n${it.nursingManagementBn}") }
                    ?: bestDrug?.let { "${it.classification}\nIndications: ${it.indications}\nNursing: ${it.nursingResponsibilities}" }
                    ?: bestProc?.let { "${it.definition}\n\nSteps:\n${it.steps}" }
                    ?: bi(language, "Tip: Add a nursing topic keyword alongside your photo (e.g., 'Pain', 'Hypertension', 'DKA', 'IV Cannulation').", "টিপস: ছবির সাথে একটি বিষয় লিখুন (যেমন: 'ব্যথা', 'উচ্চ রক্তচাপ', 'ডায়াবেটিস')।")
            )
        )
        return NursingAiResponse(
            summary = bi(language, "Image Search — $matchedSummary", "ছবিভিত্তিক প্রশ্ন — $matchedSummary"),
            sections = sections,
            citations = listOfNotNull(bestDoc?.let { NursingCitation(it.sourceTitle, it.sourceUrl, it.publishedDate, it.verificationStatus) }),
            relatedTopics = listOf("Definition of Nursing", "Definition of Pain", "Vital Signs"),
            emergencyWarning = emergencyWarning,
            firebaseNotice = noticeFor(language)
        )
    }

    private fun buildGeneralEducationalFallback(
        query: String, lang: AppLanguage, sampleDocs: List<KnowledgeDocEntity>, emergencyWarning: String
    ): NursingAiResponse {
        val sections = listOf(
            NursingSection(
                bi(lang, "1. Study Guide for \"$query\" (ADPIE Format)", "১. \"$query\" অধ্যয়নের কাঠামো (ADPIE)"),
                bi(
                    lang,
                    "No exact offline monograph matched \"$query\" yet in local storage (live open-ended AI answers require Firebase AI Logic). Use this ADPIE framework to structure your answer:\n• 1. Definition & Etiology\n• 2. Pathophysiology & Clinical Signs\n• 3. Focused Assessment (ABCDE + Vital Signs)\n• 4. Medical & Nursing Management (NANDA-I / NIC / NOC)\n• 5. Complications, Prevention & Patient Education.",
                    "\"$query\" বিষয়ের সরাসরি অফলাইন মনোগ্রাফ পাওয়া যায়নি (লাইভ ওপেন-এন্ডেড AI উত্তরের জন্য Firebase প্রয়োজন)। নিচের ADPIE কাঠামো অনুযায়ী উত্তর সাজান:\n• ১. সংজ্ঞা ও কারণ\n• ২. প্যাথোফিজিওলজি ও লক্ষণ\n• ৩. মূল্যায়ন (ABCDE ও ভাইটাল সাইন)\n• ৪. মেডিকেল ও নার্সিং ব্যবস্থাপনা\n• ৫. জটিলতা, প্রতিরোধ ও রোগীর শিক্ষা।"
                )
            ),
            NursingSection(
                bi(lang, "2. Suggested Verified Topics Available Offline", "২. অফলাইনে উপলব্ধ যাচাইকৃত টপিকসমূহ"),
                sampleDocs.joinToString("\n") { "• ${bi(lang, it.titleEn, it.titleBn)}" } +
                    bi(
                        lang,
                        "\n\nTry searching: Definition of Nursing, Definition of Pain, Fever, Shock, Myocardial Infarction, Stroke, Asthma/COPD, Burns, Blood Transfusion, Vital Signs, UTI/CKD, Anemia, Pressure Ulcer, Normal Labor.",
                        "\n\nখুঁজে দেখুন: নার্সিং কাকে বলে, ব্যথা কাকে বলে, জ্বর, শক, হার্ট অ্যাটাক, স্ট্রোক, অ্যাজমা, ডেঙ্গু, রক্ত সঞ্চালন, ভাইটাল সাইন, অ্যানিমিয়া, বেড সোর।"
                    )
            )
        )
        return NursingAiResponse(
            summary = bi(lang, "Nursing Study Framework: $query", "নার্সিং শিক্ষামূলক নির্দেশিকা: $query"),
            sections = sections,
            citations = listOf(NursingCitation("Potter & Perry's Fundamentals of Nursing & WHO Guidelines", "https://www.who.int", "2024-11-01", "Verified Framework")),
            relatedTopics = listOf("Definition of Nursing", "Definition of Pain", "Vital Signs", "Shock & Sepsis"),
            emergencyWarning = emergencyWarning,
            firebaseNotice = noticeFor(lang)
        )
    }

    private data class CarePlanTuple(
        val diagEn: String, val diagBn: String, val goalsEn: String, val goalsBn: String, val intervEn: String, val intervBn: String
    )

    companion object {
        private val genericNursingWords = setOf(
            "nursing", "nurse", "patient", "clinical", "management", "care", "disease",
            "syndrome", "treatment", "hospital", "medical", "নার্সিং", "রোগী", "চিকিৎসা", "ব্যবস্থাপনা"
        )
    }
}

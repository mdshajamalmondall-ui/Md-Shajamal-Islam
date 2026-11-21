package com.example.data.ai

import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.data.local.db.ChatMessageEntity
import com.example.data.local.db.DrugEntity
import com.example.data.local.db.KnowledgeDocEntity
import com.example.data.local.db.NursingDatabase
import com.example.data.local.db.ProcedureEntity
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
        attachedImageLabel: String?
    ): NursingAiResponse {
        delay(350) // Natural processing pause for responsiveness

        val effectiveLanguage = detectEffectiveLanguage(query, language)
        val combinedQuery = buildContextAwareQuery(query, history, attachedImageLabel)
        val tokens = tokenize(combinedQuery)

        val docs = db.knowledgeDocDao().getAllOnce()
        val drugs = db.drugDao().getAllOnce()
        val procedures = db.procedureDao().getAllOnce()

        val bestDrug = scoreDrugs(drugs, tokens)
        val bestProc = scoreProcedures(procedures, tokens)
        val bestDoc = scoreDocs(docs, tokens)

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

        // Route to drug monograph if drug score is strongest
        if (bestDrug != null && bestDrug.second >= (bestDoc?.second ?: 0) && bestDrug.second >= (bestProc?.second ?: 0)) {
            return formatDrugResponse(bestDrug.first, query, effectiveLanguage, emergencyWarning)
        }

        // Route to procedure guide if procedure score is strongest
        if (bestProc != null && bestProc.second >= (bestDoc?.second ?: 0)) {
            return formatProcedureResponse(bestProc.first, query, effectiveLanguage, emergencyWarning)
        }

        // Route to structured Knowledge Document with dynamic section filtering
        if (bestDoc != null && bestDoc.second > 0) {
            return formatKnowledgeDocResponse(bestDoc.first, query, effectiveLanguage, emergencyWarning)
        }

        // Fallback structured nursing educational response when query is broad or new
        return buildGeneralEducationalFallback(query, effectiveLanguage, docs.take(2), emergencyWarning)
    }

    override suspend fun generateCarePlan(
        patientCase: String,
        language: AppLanguage
    ): NursingAiResponse {
        delay(400)
        val lang = detectEffectiveLanguage(patientCase, language)
        val tokens = tokenize(patientCase)
        val matchedDoc = scoreDocs(db.knowledgeDocDao().getAllOnce(), tokens)?.first

        val isResp = tokens.any { it in setOf("dyspnea", "spo2", "copd", "asthma", "pneumonia", "cough", "breathing", "শ্বাসকষ্ট", "নিউমোনিয়া") }
        val isCardiac = tokens.any { it in setOf("bp", "hypertension", "chest", "cardiac", "heart", "edema", "রক্তচাপ", "হার্ট") }
        val isMaternal = tokens.any { it in setOf("pregnant", "pph", "bleeding", "postpartum", "eclampsia", "seizure", "প্রসব", "এক্লাম্পসিয়া") }

        val (diagnosisEn, diagnosisBn, goalsEn, goalsBn, intervEn, intervBn) = when {
            isMaternal -> CarePlanTuple(
                "1. Deficient Fluid Volume / Risk for Maternal Shock (NANDA-I)\n2. Risk for Injury related to eclamptic seizures",
                "১. রক্তক্ষরণজনিত ফ্লুইড ভলিউম ঘাটতি (NANDA-I)\n২. এক্লাম্পটিক খিঁচুনিজনিত আঘাতের ঝুঁকি",
                "• NOC: Maintain SBP >= 90 mmHg, firm uterine fundus, and urine output >= 30 mL/hr within 1 hour.\n• No seizure-related trauma.",
                "• NOC: ১ ঘণ্টার মধ্যে রক্তচাপ স্থিতিশীল রাখা, জরায়ু শক্ত রাখা ও প্রস্রাব >= ৩০ mL/hr নিশ্চিত করা।",
                "• Perform firm fundal massage & quantify blood loss (QBL).\n• Establish two 16–18G IV lines and administer prescribed Oxytocin/MgSO4.\n• Monitor DTRs, RR (>=12/min), and keep Calcium Gluconate at bedside.",
                "• ফান্ডাল ম্যাসাজ ও রক্তক্ষরণ পরিমাপ করুন।\n• দুটি ১৬–১৮G IV ক্যানুলা স্থাপন করে অক্সিটোসিন/MgSO4 দিন।\n• রিফ্লেক্স, শ্বাসের হার ও ক্যালসিয়াম গ্লুকোনেট হাতের কাছে রাখুন।"
            )
            isResp -> CarePlanTuple(
                "1. Impaired Gas Exchange (NANDA-I) related to alveolar exudate/ventilation-perfusion mismatch\n2. Ineffective Airway Clearance related to excessive secretions",
                "১. গ্যাস বিনিময়ে ব্যাঘাত (NANDA-I)\n২. অতিরিক্ত কফের কারণে শ্বাসনালী পরিষ্কারে অক্ষমতা",
                "• NOC: Maintain SpO2 >= 94% (88–92% if COPD) and RR 12–20/min within 2–4 hours.\n• Effective clearance of secretions.",
                "• NOC: ২–৪ ঘণ্টার মধ্যে SpO2 >= ৯৪% (COPD-তে ৮৮–৯২%) এবং স্বাভাবিক শ্বাসের হার বজায় রাখা।",
                "• Position in Semi-Fowler's (30–45°).\n• Administer titrated supplemental O2 & prescribed bronchodilators.\n• Encourage deep breathing, coughing, and hydration (if not restricted).",
                "• রোগীকে সেমি-ফাউলার পজিশনে (৩০–৪৫°) রাখুন।\n• নির্ধারিত মাত্রায় অক্সিজেন ও ব্রঙ্কোডাইলেটর দিন।\n• গভীর শ্বাস-প্রশ্বাস ও কাশির ব্যায়াম শেখান।"
            )
            isCardiac -> CarePlanTuple(
                "1. Decreased Cardiac Output / Risk for Ineffective Tissue Perfusion (NANDA-I)\n2. Activity Intolerance",
                "১. কার্ডিয়াক আউটপুট হ্রাস / টিস্যু পারফিউশনের ঝুঁকি (NANDA-I)\n২. শারীরিক পরিশ্রমে অসহিষ্ণুতা",
                "• NOC: Stabilize BP within target range, absence of chest pain, clear lung sounds, and urine output >= 30 mL/hr.",
                "• NOC: রক্তচাপ নিয়ন্ত্রণে রাখা, বুকে ব্যথা না থাকা এবং ফুসফুস পরিষ্কার থাকা।",
                "• Monitor vital signs, 12-lead ECG, and daily weights.\n• Administer ordered antihypertensives/diuretics after checking BP and K+.\n• Elevate head of bed and teach DASH low-sodium diet (<2g/day).",
                "• নিয়মিত রক্তচাপ, ইসিজি ও দৈনিক ওজন পর্যবেক্ষণ করুন।\n• BP ও পটাশিয়াম দেখে ওষুধ প্রয়োগ করুন।\n• কম লবণযুক্ত DASH ডায়েট সম্পর্কে শিক্ষা দিন।"
            )
            else -> CarePlanTuple(
                "1. Acute Knowledge Deficit / Self-Health Management Readiness (NANDA-I)\n2. Risk for Complications related to acute clinical presentation",
                "১. স্বাস্থ্য ব্যবস্থাপনা সম্পর্কিত জ্ঞানের ঘাটতি (NANDA-I)\n২. রোগের জটিলতার ঝুঁকি",
                "• NOC: Patient/caregiver verbalizes 3 red-flag symptoms and demonstrates safe care plan adherence prior to discharge.",
                "• NOC: রোগী ও পরিবার ৩টি বিপদচিহ্ন চিনতে পারবে এবং সঠিক যত্ন পদ্ধতি বলতে পারবে।",
                "• Complete focused head-to-toe & vital signs assessment.\n• Implement evidence-based NIC interventions tailored to diagnosis.\n• Provide teach-back bilingual education and medication safety counseling.",
                "• সম্পূর্ণ হেড-টু-টো ও ভাইটাল সাইন মূল্যায়ন করুন।\n• প্রমাণ-ভিত্তিক NIC নার্সিং হস্তক্ষেপ প্রয়োগ করুন।\n• টিচ-ব্যাক পদ্ধতিতে রোগীকে শিক্ষা দিন।"
            )
        }

        val sections = listOf(
            NursingSection(
                bi(lang, "1. Patient Assessment", "১. রোগীর মূল্যায়ন (Assessment)"),
                bi(
                    lang,
                    "Case Input: $patientCase\nFocus Cues: Vital signs (BP, HR, RR, Temp, SpO2), focused system exam, lab trends, allergies, and psychosocial readiness.",
                    "রোগীর তথ্য: $patientCase\nমূল্যায়ন: ভাইটাল সাইন, সিস্টেম-ভিত্তিক পরীক্ষা, ল্যাব রিপোর্ট এবং অ্যালার্জি ইতিহাস।"
                )
            ),
            NursingSection(
                bi(lang, "2. Nursing Diagnosis (NANDA-I Framework)", "২. নার্সিং ডায়াগনোসিস (NANDA-I)"),
                bi(lang, diagnosisEn, diagnosisBn)
            ),
            NursingSection(
                bi(lang, "3. Goals & Expected Outcomes (NOC)", "৩. লক্ষ্য ও প্রত্যাশিত ফলাফল (NOC)"),
                bi(lang, goalsEn, goalsBn)
            ),
            NursingSection(
                bi(lang, "4. Nursing Interventions (NIC)", "৪. নার্সিং হস্তক্ষেপ (NIC)"),
                bi(lang, intervEn, intervBn)
            ),
            NursingSection(
                bi(lang, "5. Clinical Rationales", "৫. বৈজ্ঞানিক যুক্তি (Rationales)"),
                matchedDoc?.let { bi(lang, it.interventionsRationaleEn, it.interventionsRationaleBn) }
                    ?: bi(
                        lang,
                        "• Systematic ABCDE prioritization stabilizes airway, breathing, and circulation first.\n• Monitoring vital trends and labs detects deterioration early.\n• Teach-back education lowers readmission risk.",
                        "• ABCDE অগ্রাধিকার শ্বাসনালী, শ্বাস ও রক্তসঞ্চালন সুরক্ষিত রাখে।\n• নিয়মিত পর্যবেক্ষণ জটিলতা দ্রুত শনাক্ত করে।"
                    )
            ),
            NursingSection(
                bi(lang, "6. Evaluation", "৬. মূল্যায়ন (Evaluation)"),
                bi(
                    lang,
                    "Reassess vital signs, symptom relief, and NOC outcome indicators at scheduled intervals; document met/partially met/unmet goals and revise care plan.",
                    "নির্ধারিত সময় পরপর ভাইটাল সাইন ও লক্ষণ পুনরায় মূল্যায়ন করে লক্ষ্য অর্জিত হয়েছে কিনা নথিভুক্ত করুন।"
                )
            )
        )

        val citations = listOfNotNull(
            matchedDoc?.let {
                NursingCitation(it.sourceTitle, it.sourceUrl, it.publishedDate, it.verificationStatus)
            },
            NursingCitation(
                title = "NANDA-I, NIC & NOC Educational Nursing Taxonomy (Licensed framework summary)",
                url = "https://nanda.org",
                publishedDate = "2024-12-01",
                verificationStatus = "Educational Reference"
            )
        )

        return NursingAiResponse(
            summary = bi(
                lang,
                "Educational Nursing Care Plan (Sample Template — requires Firebase for live AI synthesis).",
                "শিক্ষামূলক নার্সিং কেয়ার প্ল্যান (নমুনা টেমপ্লেট — লাইভ AI-এর জন্য Firebase প্রয়োজন)।"
            ),
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
        delay(350)
        val lang = detectEffectiveLanguage(input, language)
        val baseRag = askNursingQuestion(input, emptyList(), lang)

        val toolSectionTitle = bi(lang, "Smart Tool Output — ${toolType.titleEn}", "স্মার্ট টুল ফলাফল — ${toolType.titleBn}")
        val customBody = when (toolType) {
            SmartAiToolType.TRANSLATOR -> bi(
                lang,
                "Bilingual Clinical Translation Guide:\n• Dyspnea = শ্বাসকষ্ট (Shortness of breath)\n• Hypertension = উচ্চ রক্তচাপ (High blood pressure)\n• Hypoglycemia = রক্তে শর্করা কমে যাওয়া (Low blood sugar <70 mg/dL)\n• Cyanosis = অক্সিজেনের অভাবে ত্বক/ঠোঁট নীলচে হওয়া\n• Asepsis = জীবাণুমুক্ত অবস্থা\n• Edema = শরীরে পানি জমা / ফুলে যাওয়া",
                "দ্বিভাষিক ক্লিনিক্যাল অনুবাদ গাইড:\n• Dyspnea = শ্বাসকষ্ট\n• Hypertension = উচ্চ রক্তচাপ\n• Hypoglycemia = রক্তে গ্লুকোজ <৭০ mg/dL হয়ে যাওয়া\n• Cyanosis = ত্বক ও ঠোঁট নীলচে হয়ে যাওয়া\n• Asepsis = জীবাণুমুক্ত পদ্ধতি\n• Edema = টিস্যুতে পানি জমা (শোথ)"
            )
            SmartAiToolType.MCQ_GEN -> bi(
                lang,
                "Sample Exam MCQ (Topic: ${input.take(50)}):\nQ1. Which nursing action takes FIRST priority when a patient presents with acute respiratory distress (SpO2 87%)?\nA) Obtain chest X-ray first\nB) Elevate head of bed to High-Fowler's & apply ordered oxygen ✓\nC) Collect sputum culture\nD) Document vital signs only\nRationale: Airway & Breathing (ABCs) always precede diagnostic tasks.",
                "নমুনা MCQ প্রশ্ন:\nপ্রশ্ন: রোগীর তীব্র শ্বাসকষ্ট (SpO2 ৮৭%) হলে নার্সের প্রথম কাজ কী?\nA) আগে এক্স-রে করানো\nB) বিছানার মাথা উঁচু করে (High-Fowler's) নির্ধারিত অক্সিজেন দেওয়া ✓\nC) কফ সংগ্রহ করা\nD) শুধু নোট লেখা\nব্যাখ্যা: ABC নীতি অনুযায়ী শ্বাসনালী ও অক্সিজেন সর্বদা প্রথম অগ্রাধিকার।"
            )
            SmartAiToolType.VIVA_PRACTICE -> bi(
                lang,
                "Examiner Viva Q&A Simulation:\n• Examiner: 'State the 5 Rights + 5 Extended Rights of Medication Administration.'\n• Model Answer: Right Patient (2 identifiers), Drug, Dose, Route, Time + Reason, Assessment, Documentation, Evaluation, Right to Refuse/Education.\n• Follow-up: 'What is the antidote for Magnesium Sulfate toxicity?' -> 10% Calcium Gluconate 1 g IV slowly.",
                "ভাইভা অনুশীলন:\n• পরীক্ষক: 'ওষুধ প্রয়োগের মূল অধিকারগুলো বলুন।'\n• আদর্শ উত্তর: সঠিক রোগী, ওষুধ, ডোজ, পথ, সময়, কারণ, মূল্যায়ন, নথিভুক্তকরণ, প্রতিক্রিয়া ও প্রত্যাখ্যানের অধিকার।\n• ফলো-আপ: 'MgSO4 বিষক্রিয়ার অ্যান্টিডোট কী?' -> ১০% ক্যালসিয়াম গ্লুকোনেট।"
            )
            else -> baseRag.sections.take(3).joinToString("\n\n") { "${it.title}:\n${it.body}" }
        }

        val combinedSections = listOf(NursingSection(toolSectionTitle, customBody)) + baseRag.sections.take(3)
        return baseRag.copy(
            summary = bi(
                lang,
                "${toolType.titleEn} (Sample & Grounded Reference Mode)",
                "${toolType.titleBn} (নমুনা ও যাচাইকৃত রেফারেন্স মোড)"
            ),
            sections = combinedSections,
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun buildContextAwareQuery(
        query: String,
        history: List<ChatMessageEntity>,
        attachedImageLabel: String?
    ): String {
        val followUpHints = listOf(
            "complication", "rationale", "prevention", "dose", "dosage", "nursing",
            "treatment", "cause", "symptom", "more", "explain", "its", "this",
            "জটিলতা", "করণীয়", "ওষুধ", "লক্ষণ", "প্রতিরোধ", "বিস্তারিত"
        )
        val isShortFollowUp = query.split(" ").size <= 6 &&
            followUpHints.any { query.lowercase().contains(it) }
        val prevUserMsg = history.lastOrNull { it.role == "user" }?.content.orEmpty()
        return listOfNotNull(
            if (isShortFollowUp && prevUserMsg.isNotBlank()) prevUserMsg else null,
            query,
            attachedImageLabel
        ).joinToString(" ")
    }

    private fun detectEffectiveLanguage(query: String, pref: AppLanguage): AppLanguage {
        val hasBengaliScript = query.any { it in '\u0980'..'\u09FF' }
        return if (hasBengaliScript) AppLanguage.BENGALI else pref
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\u0980-\\u09FF\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 2 }
    }

    private fun scoreDocs(docs: List<KnowledgeDocEntity>, tokens: List<String>): Pair<KnowledgeDocEntity, Int>? {
        return docs.map { doc ->
            val haystack = "${doc.titleEn} ${doc.titleBn} ${doc.keywords} ${doc.subject} ${doc.definitionEn} ${doc.definitionBn}".lowercase()
            var score = 0
            for (t in tokens) {
                if (doc.keywords.lowercase().contains(t)) score += 4
                if (doc.titleEn.lowercase().contains(t) || doc.titleBn.lowercase().contains(t)) score += 5
                if (haystack.contains(t)) score += 1
            }
            doc to score
        }.filter { it.second > 0 }.maxByOrNull { it.second }
    }

    private fun scoreDrugs(drugs: List<DrugEntity>, tokens: List<String>): Pair<DrugEntity, Int>? {
        return drugs.map { drug ->
            val haystack = "${drug.genericName} ${drug.brandNames} ${drug.classification} ${drug.indications}".lowercase()
            var score = 0
            for (t in tokens) {
                if (drug.genericName.lowercase().contains(t) || drug.brandNames.lowercase().contains(t)) score += 7
                if (haystack.contains(t)) score += 1
            }
            drug to score
        }.filter { it.second >= 5 }.maxByOrNull { it.second }
    }

    private fun scoreProcedures(procs: List<ProcedureEntity>, tokens: List<String>): Pair<ProcedureEntity, Int>? {
        return procs.map { proc ->
            val haystack = "${proc.titleEn} ${proc.titleBn} ${proc.category} ${proc.definition}".lowercase()
            var score = 0
            for (t in tokens) {
                if (proc.titleEn.lowercase().contains(t) || proc.titleBn.lowercase().contains(t)) score += 6
                if (haystack.contains(t)) score += 1
            }
            proc to score
        }.filter { it.second >= 5 }.maxByOrNull { it.second }
    }

    private fun detectEmergency(query: String, doc: KnowledgeDocEntity?, lang: AppLanguage): String {
        val q = query.lowercase()
        val urgentTerms = listOf("chest pain", "unresponsive", "anaphylaxis", "seizure", "bleeding", "suicide", "arrest", "stridor", "shock", "খিঁচুনি", "রক্তপাত", "বুকে ব্যথা")
        if (urgentTerms.any { q.contains(it) }) {
            return doc?.emergencyFlag?.takeIf { it.isNotBlank() } ?: bi(
                lang,
                "Clinical Safety Alert: Symptoms mentioned may indicate a medical emergency. Seek immediate emergency medical evaluation or follow institutional rapid-response protocols.",
                "জরুরি সতর্কতা: উল্লিখিত লক্ষণগুলো জীবন-হুমকিপূর্ণ হতে পারে। অবিলম্বে জরুরি চিকিৎসা সেবা বা হাসপাতালের ইমার্জেন্সি প্রোটোকল অনুসরণ করুন।"
            )
        }
        return doc?.emergencyFlag.orEmpty()
    }

    private fun formatKnowledgeDocResponse(
        doc: KnowledgeDocEntity,
        query: String,
        lang: AppLanguage,
        emergencyWarning: String
    ): NursingAiResponse {
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

        // Dynamically filter sections if the user asked a focused follow-up question
        val focused = when {
            q.contains("complication") || q.contains("জটিলতা") -> allSections.filterIndexed { i, _ -> i in listOf(0, 10, 13) }
            q.contains("symptom") || q.contains("sign") || q.contains("লক্ষণ") -> allSections.filterIndexed { i, _ -> i in listOf(0, 3, 5, 13) }
            q.contains("cause") || q.contains("risk") || q.contains("কারণ") -> allSections.filterIndexed { i, _ -> i in listOf(0, 2, 4, 11) }
            q.contains("nursing") || q.contains("intervention") || q.contains("নার্সিং") -> allSections.filterIndexed { i, _ -> i in listOf(5, 8, 9, 12, 13) }
            else -> allSections
        }

        return NursingAiResponse(
            summary = bi(lang, doc.titleEn, doc.titleBn),
            sections = focused,
            citations = listOf(
                NursingCitation(doc.sourceTitle, doc.sourceUrl, doc.publishedDate, doc.verificationStatus)
            ),
            relatedTopics = listOf(
                bi(lang, "${doc.subject} MCQs", "${doc.subject} MCQ অনুশীলন"),
                bi(lang, "Generate Nursing Care Plan", "নার্সিং কেয়ার প্ল্যান তৈরি করুন"),
                bi(lang, "Check Drug Interactions", "ওষুধের তথ্য দেখুন")
            ),
            emergencyWarning = emergencyWarning,
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun formatDrugResponse(
        drug: DrugEntity,
        query: String,
        lang: AppLanguage,
        emergencyWarning: String
    ): NursingAiResponse {
        val sections = listOf(
            NursingSection(
                bi(lang, "1. Drug Overview & Classification", "১. ওষুধের পরিচিতি ও শ্রেণিবিভাগ"),
                "${drug.genericName} (${drug.brandNames})\nClass: ${drug.classification}"
            ),
            NursingSection(
                bi(lang, "2. Indications & Mechanism of Action", "২. ব্যবহার ও কার্যপদ্ধতি"),
                "Indications: ${drug.indications}\nMOA: ${drug.mechanismOfAction}"
            ),
            NursingSection(
                bi(lang, "3. Dosage Reference & Route", "৩. ডোজ ও প্রয়োগের পথ"),
                "Dosage: ${drug.dosageReference}\nRoute: ${drug.route}\n(Safety: Always verify institutional order & patient renal/hepatic status.)"
            ),
            NursingSection(
                bi(lang, "4. Contraindications & Side Effects / ADRs", "৪. প্রতিনির্দেশনা ও পার্শ্বপ্রতিক্রিয়া"),
                "Contraindications: ${drug.contraindications}\nSide Effects: ${drug.sideEffects}\nSerious ADRs: ${drug.adverseReactions}"
            ),
            NursingSection(
                bi(lang, "5. Nursing Responsibilities & Counseling", "৫. নার্সিং দায়িত্ব ও রোগীর পরামর্শ"),
                "Nursing Care: ${drug.nursingResponsibilities}\nInteractions: ${drug.drugInteractions}\nCounseling: ${drug.patientCounseling}\nStorage: ${drug.storageInfo}"
            )
        )
        return NursingAiResponse(
            summary = "${drug.genericName} — ${drug.classification}",
            sections = sections,
            citations = listOf(
                NursingCitation(drug.sourceTitle, "https://www.who.int/groups/expert-committee-on-selection-and-use-of-essential-medicines", drug.updatedDate, drug.verificationStatus)
            ),
            relatedTopics = listOf("Dosage Calculator", "10 Rights of Medication", "High-Alert Drug Safety"),
            emergencyWarning = if (drug.isHighAlert) {
                bi(
                    lang,
                    "HIGH-ALERT MEDICATION: Independent double-check required prior to administration. Never substitute for clinical verification.",
                    "হাই-অ্যালার্ট ওষুধ: প্রয়োগের আগে আরেকজন নার্সের সাথে ডোজ ও নাম পুনরায় যাচাই করুন।"
                )
            } else emergencyWarning,
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun formatProcedureResponse(
        proc: ProcedureEntity,
        query: String,
        lang: AppLanguage,
        emergencyWarning: String
    ): NursingAiResponse {
        val sections = listOf(
            NursingSection(bi(lang, "1. Definition & Purpose", "১. সংজ্ঞা ও উদ্দেশ্য"), "${proc.definition}\n\nPurpose: ${proc.purpose}"),
            NursingSection(bi(lang, "2. Equipment & Preparation", "২. প্রয়োজনীয় সরঞ্জাম ও প্রস্তুতি"), "Equipment: ${proc.equipment}\n\nPreparation: ${proc.preparation}"),
            NursingSection(bi(lang, "3. Step-by-Step Procedure", "৩. ধাপে ধাপে পদ্ধতি"), proc.steps),
            NursingSection(bi(lang, "4. Nursing Responsibilities & Infection Control", "৪. নার্সিং দায়িত্ব ও সংক্রমণ নিয়ন্ত্রণ"), "Responsibilities: ${proc.nursingResponsibilities}\n\nInfection Control: ${proc.infectionControl}"),
            NursingSection(bi(lang, "5. Complications & Documentation", "৫. জটিলতা ও নথিভুক্তকরণ"), "Complications: ${proc.complications}\n\nDocumentation: ${proc.documentation}")
        )
        val supervisionBanner = if (proc.supervisedTrainingRequired) {
            bi(
                lang,
                "Clinical Supervision Notice: This invasive/clinical skill requires supervised hands-on training and competency validation.",
                "প্রশিক্ষণ সতর্কতা: এই ক্লিনিক্যাল পদ্ধতিটি তত্ত্বাবধানে হাতে-কলমে প্রশিক্ষণ ছাড়া প্রয়োগ করা উচিত নয়।"
            )
        } else emergencyWarning

        return NursingAiResponse(
            summary = bi(lang, proc.titleEn, proc.titleBn),
            sections = sections,
            citations = listOf(
                NursingCitation(proc.sourceTitle, "https://www.who.int", proc.updatedDate, proc.verificationStatus)
            ),
            relatedTopics = listOf("WHO 5 Moments of Hand Hygiene", "PPE Guidelines", "Documentation Standards"),
            emergencyWarning = supervisionBanner,
            firebaseNotice = noticeFor(lang)
        )
    }

    private fun buildImageGroundedResponse(
        query: String,
        imageLabel: String,
        bestDoc: KnowledgeDocEntity?,
        bestDrug: DrugEntity?,
        bestProc: ProcedureEntity?,
        language: AppLanguage,
        emergencyWarning: String
    ): NursingAiResponse {
        val matchedSummary = bestDoc?.let { bi(language, it.titleEn, it.titleBn) }
            ?: bestDrug?.genericName
            ?: bestProc?.let { bi(language, it.titleEn, it.titleBn) }
            ?: bi(language, "Uploaded Nursing Study Material", "আপলোডকৃত নার্সিং স্টাডি ছবি")

        val sections = listOf(
            NursingSection(
                bi(language, "1. Image Analysis Status (Sample / Local Mode)", "১. ছবি বিশ্লেষণ অবস্থা (নমুনা মোড)"),
                bi(
                    language,
                    "Attached image: $imageLabel\nPrompt context: ${query.ifBlank { "Analyze this nursing study image" }}\n\nNote: Live multimodal vision analysis of book pages or handwritten notes requires Firebase AI Logic (Gemini). Below is a structured educational template matched to your text query.",
                    "সংযুক্ত ছবি: $imageLabel\nপ্রশ্ন: ${query.ifBlank { "ছবিটি ব্যাখ্যা করো" }}\n\nবিঃদ্রঃ বই বা নোটের ছবির সরাসরি ভিশন বিশ্লেষণের জন্য Firebase AI Logic প্রয়োজন। নিচে আপনার প্রশ্নের সাথে সম্পর্কিত শিক্ষামূলক কাঠামো দেওয়া হলো।"
                )
            ),
            NursingSection(
                bi(language, "2. Structured Study Breakdown ($matchedSummary)", "২. সম্পর্কিত নার্সিং ব্যাখ্যা ($matchedSummary)"),
                bestDoc?.let { bi(language, "${it.definitionEn}\n\nKey Nursing Management:\n${it.nursingManagementEn}", "${it.definitionBn}\n\nনার্সিং ব্যবস্থাপনা:\n${it.nursingManagementBn}") }
                    ?: bestDrug?.let { "${it.classification}\nIndications: ${it.indications}\nNursing Responsibilities: ${it.nursingResponsibilities}" }
                    ?: bestProc?.let { "${it.definition}\n\nSteps:\n${it.steps}" }
                    ?: bi(
                        language,
                        "Tip: Add a keyword in the search bar alongside your photo (e.g., 'Hypertension', 'DKA', 'IV Cannulation', 'Ceftriaxone') for targeted local clinical notes.",
                        "টিপস: ছবির সাথে একটি বিষয় লিখুন (যেমন: 'উচ্চ রক্তচাপ', 'ডায়াবেটিস', 'IV ক্যানুলেশন') যাতে প্রাসঙ্গিক নার্সিং নোট পাওয়া যায়।"
                    )
            )
        )

        return NursingAiResponse(
            summary = bi(language, "Image Question Search — $matchedSummary", "ছবিভিত্তিক প্রশ্ন — $matchedSummary"),
            sections = sections,
            citations = listOfNotNull(
                bestDoc?.let { NursingCitation(it.sourceTitle, it.sourceUrl, it.publishedDate, it.verificationStatus) }
            ),
            relatedTopics = listOf("Ask Follow-up Question", "Summarize Into Flashcard", "Generate MCQ"),
            emergencyWarning = emergencyWarning,
            firebaseNotice = noticeFor(language)
        )
    }

    private fun buildGeneralEducationalFallback(
        query: String,
        lang: AppLanguage,
        sampleDocs: List<KnowledgeDocEntity>,
        emergencyWarning: String
    ): NursingAiResponse {
        val sections = listOf(
            NursingSection(
                bi(lang, "1. Clinical Nursing Framework (ADPIE)", "১. ক্লিনিক্যাল নার্সিং কাঠামো (ADPIE)"),
                bi(
                    lang,
                    "Topic queried: \"$query\"\n\nFor structured nursing study, organize this topic across:\n• Definition & Pathophysiology\n• Priority Assessment (ABCs + Vital Signs + Red Flags)\n• Medical & Nursing Management (NANDA-I / NIC / NOC)\n• Medication Safety (10 Rights) & Patient Education.",
                    "আপনার প্রশ্ন: \"$query\"\n\nনার্সিং শিক্ষার আদর্শ কাঠামো অনুযায়ী বিষয়টি এভাবে অধ্যয়ন করুন:\n• সংজ্ঞা ও প্যাথোফিজিওলজি\n• প্রাথমিক মূল্যায়ন (ABC ও ভাইটাল সাইন)\n• মেডিকেল ও নার্সিং ব্যবস্থাপনা\n• ওষুধের নিরাপত্তা ও রোগীর শিক্ষা।"
                )
            ),
            NursingSection(
                bi(lang, "2. Available Verified Local Topics", "২. স্থানীয় যাচাইকৃত টপিকসমূহ"),
                sampleDocs.joinToString("\n") { "• ${bi(lang, it.titleEn, it.titleBn)} (${it.subject})" } +
                    bi(
                        lang,
                        "\n\nTry searching: Hypertension, Diabetes/DKA, Pneumonia, PPH/Eclampsia, Pediatric Dehydration, CPR/Anaphylaxis, Therapeutic Communication, Ceftriaxone, Heparin, IV Cannulation.",
                        "\n\nঅনুসন্ধান করে দেখুন: উচ্চ রক্তচাপ, ডায়াবেটিস, নিউমোনিয়া, PPH/এক্লাম্পসিয়া, শিশুর ডিহাইড্রেশন, CPR, অ্যানাফাইল্যাক্সিস, ইনসুলিন।"
                    )
            )
        )

        return NursingAiResponse(
            summary = bi(lang, "Nursing Educational Guidance: $query", "নার্সিং শিক্ষামূলক নির্দেশিকা: $query"),
            sections = sections,
            citations = listOf(
                NursingCitation(
                    title = "Potter & Perry's Fundamentals of Nursing & WHO Clinical Guidelines",
                    url = "https://www.who.int/teams/health-workforce/nursing-and-midwifery",
                    publishedDate = "2024-11-01",
                    verificationStatus = "Verified Framework"
                )
            ),
            relatedTopics = listOf("Hypertension", "Diabetes & DKA", "IV Cannulation", "PPH & Eclampsia"),
            emergencyWarning = emergencyWarning,
            firebaseNotice = noticeFor(lang)
        )
    }

    private data class CarePlanTuple(
        val diagEn: String,
        val diagBn: String,
        val goalsEn: String,
        val goalsBn: String,
        val intervEn: String,
        val intervBn: String
    )
}

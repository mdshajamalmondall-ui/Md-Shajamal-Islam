package com.example.data.local.db

object StudyAndMcqSeedData {

    fun initialStudyItems(): List<StudyItemEntity> = listOf(
        // Nursing Notes
        StudyItemEntity(
            id = "note_nursing_process",
            itemType = "NOTE",
            subject = "Fundamentals of Nursing",
            titleEn = "The 5-Step Nursing Process (ADPIE)",
            titleBn = "নার্সিং প্রক্রিয়ার ৫টি ধাপ (ADPIE)",
            bodyEn = "1. Assessment: Subjective (symptoms) + Objective (vitals, labs, exam).\n2. Diagnosis: NANDA-I clinical judgment (Problem + Etiology + Defining Characteristics).\n3. Planning: SMART goals & NOC outcomes.\n4. Implementation: Independent, dependent, and collaborative NIC interventions.\n5. Evaluation: Measure goal attainment and revise plan.",
            bodyBn = "১. অ্যাসেসমেন্ট (মূল্যায়ন): সাবজেক্টিভ ও অবজেক্টিভ তথ্য সংগ্রহ।\n২. ডায়াগনোসিস: NANDA-I ভিত্তিক রোগ নির্ণয়।\n৩. প্ল্যানিং: SMART লক্ষ্য নির্ধারণ।\n৪. ইমপ্লিমেন্টেশন: নার্সিং হস্তক্ষেপ বাস্তবায়ন।\n৫. ইভ্যালুয়েশন: ফলাফল মূল্যায়ন ও সংশোধন।",
            sourceTitle = "Potter & Perry's Fundamentals of Nursing",
            updatedDate = "2024-11-15"
        ),
        StudyItemEntity(
            id = "note_electrolytes",
            itemType = "NOTE",
            subject = "Medical-Surgical Nursing",
            titleEn = "Fluid & Electrolyte Imbalances Quick Reference",
            titleBn = "ফ্লুইড ও ইলেক্ট্রোলাইট ভারসাম্যহীনতা নোট",
            bodyEn = "• Na+ (135–145 mEq/L): Neuro status & seizure precautions.\n• K+ (3.5–5.0 mEq/L): Cardiac ECG (peaked T waves in hyperkalemia; U waves in hypokalemia). Never IV push KCl!\n• Ca2+ (8.5–10.5 mg/dL): Chvostek & Trousseau signs in hypocalcemia.\n• Mg2+ (1.5–2.5 mEq/L): Deep tendon reflexes.",
            bodyBn = "• সোডিয়াম (১৩৫–১৪৫ mEq/L): স্নায়বিক অবস্থা ও খিঁচুনি সতর্কতা।\n• পটাশিয়াম (৩.৫–৫.০ mEq/L): হৃদ্‌যন্ত্রের ইসিজি পরিবর্তন। কখনোই KCl সরাসরি IV পুশ দেবেন না!\n• ক্যালসিয়াম (৮.৫–১০.৫ mg/dL): কম হলে Chvostek ও Trousseau সাইন দেখা যায়।",
            sourceTitle = "Brunner & Suddarth's Medical-Surgical Nursing",
            updatedDate = "2024-12-10"
        ),
        // Books & Resources
        StudyItemEntity(
            id = "book_potter_perry",
            itemType = "BOOK",
            subject = "Fundamentals of Nursing",
            titleEn = "Recommended Core Nursing Textbooks & Free WHO Manuals",
            titleBn = "প্রস্তাবিত মূল নার্সিং বই ও ফ্রি WHO গাইডলাইন",
            bodyEn = "• Potter & Perry's Fundamentals of Nursing (Clinical Skills & ADPIE)\n• Brunner & Suddarth's Textbook of Medical-Surgical Nursing\n• Wong's Nursing Care of Infants and Children\n• WHO Pocket Book of Hospital Care for Children (Open Access)\n• BNMC Diploma & B.Sc. in Nursing Curriculum Guidelines",
            bodyBn = "• পটার ও পেরি: ফান্ডামেন্টালস অব নার্সিং\n• ব্রুনার ও সুডারথ: মেডিকেল-সার্জিক্যাল নার্সিং\n• ওং-এর পেডিয়াট্রিক নার্সিং\n• WHO ওপেন অ্যাক্সেস ক্লিনিক্যাল গাইডবুক ও BNMC সিলেবাস নির্দেশিকা।",
            sourceTitle = "BNMC & WHO Nursing Education Library",
            sourceUrl = "https://www.who.int/teams/health-workforce/nursing-and-midwifery",
            updatedDate = "2025-01-02"
        ),
        // Short Q&A
        StudyItemEntity(
            id = "short_qa_1",
            itemType = "SHORT_QA",
            subject = "Infection Prevention & Control",
            titleEn = "What is the difference between Medical and Surgical Asepsis?",
            titleBn = "মেডিকেল অ্যাসেপসিস ও সার্জিক্যাল অ্যাসেপসিসের পার্থক্য কী?",
            bodyEn = "Medical Asepsis (Clean Technique) reduces the number and transfer of pathogens (e.g., hand hygiene, clean gloves, oral meds). Surgical Asepsis (Sterile Technique) eliminates ALL microorganisms including spores from an area or object (e.g., Foley catheterization, OR procedures, central line dressing).",
            bodyBn = "মেডিকেল অ্যাসেপসিস (ক্লিন টেকনিক) জীবাণুর সংখ্যা ও বিস্তার কমায় (যেমন হাত ধোয়া)। সার্জিক্যাল অ্যাসেপসিস (স্টেরাইল টেকনিক) স্পোরসহ সমস্ত জীবাণু সম্পূর্ণ নির্মূল করে (যেমন ক্যাথেটার স্থাপন ও অপারেশন)।",
            sourceTitle = "CDC Core Infection Prevention Practices",
            updatedDate = "2024-11-20"
        ),
        // Broad Q&A
        StudyItemEntity(
            id = "broad_qa_1",
            itemType = "BROAD_QA",
            subject = "Medical-Surgical Nursing",
            titleEn = "Describe Pre-operative & Post-operative Nursing Management",
            titleBn = "অপারেশন-পূর্ব ও অপারেশন-পরবর্তী নার্সিং ব্যবস্থাপনা বর্ণনা করুন",
            bodyEn = "Pre-op: Verify informed consent, NPO status (6–8h solids, 2h clear liquids per ASA), baseline vitals/labs, remove jewelry/dentures, skin prep, antibiotic prophylaxis within 60 min.\nPost-op (PACU/Ward): ABCs first, lateral/recovery position until gag reflex returns, vitals q15min x4, pain & PONV control, early ambulation/DVT prophylaxis, surgical wound & drain output, return of bowel sounds and voiding within 6–8h.",
            bodyBn = "অপারেশন-পূর্ব: সম্মতিপত্র যাচাই, NPO নিশ্চিত করা, বেসলাইন ভাইটাল ও ল্যাব পরীক্ষা, প্রফিল্যাকটিক অ্যান্টিবায়োটিক।\nঅপারেশন-পরবর্তী: ABC নিশ্চিতকরণ, গ্যাগ রিফ্লেক্স না ফেরা পর্যন্ত পাশ ফিরিয়ে শোয়া, প্রতি ১৫ মিনিটে ভাইটাল, ব্যথা নিয়ন্ত্রণ, দ্রুত হাঁটাচলা (DVT প্রতিরোধ), ক্ষত ও প্রস্রাব পর্যবেক্ষণ।",
            sourceTitle = "WHO Safe Surgery Saves Lives Manual",
            updatedDate = "2024-12-01"
        ),
        // Viva Questions
        StudyItemEntity(
            id = "viva_1",
            itemType = "VIVA",
            subject = "Pharmacology",
            titleEn = "Viva: Why must Potassium Chloride (KCl) NEVER be given IV Push?",
            titleBn = "ভাইভা প্রশ্ন: পটাশিয়াম ক্লোরাইড (KCl) কেন কখনোই সরাসরি IV পুশ দেওয়া যায় না?",
            bodyEn = "Direct IV push of concentrated KCl causes sudden fatal hyperkalemia leading to myocardial depolarization arrest (asystole/VF). It must ALWAYS be diluted in IV fluid and infused slowly via pump at <=10 mEq/hr (peripheral) with ECG monitoring.",
            bodyBn = "সরাসরি শিরাপথে KCl পুশ দিলে রক্তে হঠাৎ অতিরিক্ত পটাশিয়াম বেড়ে গিয়ে হৃদ্‌যন্ত্র বন্ধ (কার্ডিয়াক অ্যারেস্ট) হয়ে রোগীর মৃত্যু হতে পারে। এটি সর্বদা স্যালাইনে মিশিয়ে পাম্পের মাধ্যমে ধীরগতিতে দিতে হয়।",
            sourceTitle = "ISMP High-Alert Medication Safety",
            updatedDate = "2025-01-05"
        ),
        // Case Study
        StudyItemEntity(
            id = "case_1",
            itemType = "CASE_STUDY",
            subject = "Critical Care Nursing",
            titleEn = "Case Study: 62-year-old Male with Acute Chest Pain & Diaphoresis",
            titleBn = "কেস স্টাডি: ৬২ বছর বয়সী পুরুষের তীব্র বুকে ব্যথা ও ঘাম",
            bodyEn = "Scenario: Mr. Rahman arrives with crushing substernal chest pain radiating to the left jaw for 40 min, BP 96/60 mmHg, HR 112 bpm, SpO2 91%, ST-elevation in V1–V4.\nPriority Actions: 12-lead ECG within 10 min, MONA-B assessment (Aspirin 300 mg chewed if no contraindication, O2 only if SpO2 <90–94%, Nitroglycerin ONLY if SBP >=90 & no PDE-5 use, Morphine cautiously), activate Cath Lab (PCI door-to-balloon <90 min).",
            bodyBn = "দৃশ্যপট: মি. রহমান ৪০ মিনিট ধরে বাম চোয়ালে ছড়িয়ে পড়া তীব্র বুকে ব্যথা ও ঘাম নিয়ে এসেছেন; BP ৯৬/৬০, SpO2 ৯১%, ইসিজিতে V1-V4 ST এলিভেশন।\nকরণীয়: ১০ মিনিটের মধ্যে ১২-লিড ইসিজি, অ্যাসপিরিন, সতর্কতার সাথে অক্সিজেন ও দ্রুত PCI/কার্ডিয়াক ইমার্জেন্সি সক্রিয় করা।",
            sourceTitle = "AHA STEMI Clinical Guidelines",
            updatedDate = "2024-11-30"
        ),
        // Assignment
        StudyItemEntity(
            id = "assign_1",
            itemType = "ASSIGNMENT",
            subject = "Community Health Nursing",
            titleEn = "Assignment Template: Family Health & Home Visit Assessment",
            titleBn = "অ্যাসাইনমেন্ট টেমপ্লেট: পারিবারিক স্বাস্থ্য ও হোম ভিজিট মূল্যায়ন",
            bodyEn = "1. Demographic Profile & Family Tree (Genogram).\n2. Environmental Sanitation (water source, latrine, vector control).\n3. Maternal, Child & Immunization Audit.\n4. Nutritional & Chronic Disease Screening (BP, BMI, Blood Glucose).\n5. Prioritized Community Nursing Diagnoses & Health Education Plan.",
            bodyBn = "১. পারিবারিক তথ্য ও জেনোগ্রাম।\n২. পরিবেশগত স্বাস্থ্যবিধি (পানির উৎস, স্যানিটেশন)।\n৩. মা ও শিশুর টিকাদান তালিকা।\n৪. পুষ্টি ও দীর্ঘস্থায়ী রোগ স্ক্রিনিং।\n৫. অগ্রাধিকারভিত্তিক কমিউনিটি নার্সিং ডায়াগনোসিস ও শিক্ষা পরিকল্পনা।",
            sourceTitle = "BNMC Community Health Nursing Practicum",
            updatedDate = "2024-10-01"
        ),
        // NANDA-I / NIC / NOC Diagnoses
        StudyItemEntity(
            id = "nanda_1",
            itemType = "NANDA",
            subject = "Nursing Care Plans",
            titleEn = "NANDA-I / NIC / NOC: Ineffective Airway Clearance",
            titleBn = "NANDA-I / NIC / NOC: শ্বাসনালী পরিষ্কারে অক্ষমতা",
            bodyEn = "NANDA-I Diagnosis: Ineffective Airway Clearance related to excessive tracheobronchial secretions as evidenced by adventitious breath sounds (crackles/rhonchi), ineffective cough, and SpO2 89%.\nNOC Outcome: Respiratory Status: Airway Patency (SpO2 >=95%, clear lung fields).\nNIC Interventions: Airway Management (semi-Fowler's, hydration, chest physiotherapy, suctioning PRN), Oxygen Therapy.",
            bodyBn = "NANDA-I ডায়াগনোসিস: অতিরিক্ত কফের কারণে শ্বাসনালী পরিষ্কারে অক্ষমতা।\nNOC ফলাফল: শ্বাসনালী পরিষ্কার ও SpO2 >= ৯৫% থাকা।\nNIC হস্তক্ষেপ: সেমি-ফাউলার পজিশন, হাইড্রেশন, চেস্ট ফিজিওথেরাপি, প্রয়োজনে সাকশনিং ও অক্সিজেন থেরাপি।",
            sourceTitle = "NANDA-I, NIC & NOC Educational Framework Reference (Licensing Notice: Educational summary)",
            updatedDate = "2024-12-15"
        ),
        // Care Plan Example
        StudyItemEntity(
            id = "careplan_1",
            itemType = "CARE_PLAN",
            subject = "Medical-Surgical Nursing",
            titleEn = "Sample Care Plan: Impaired Gas Exchange in COPD Exacerbation",
            titleBn = "নমুনা কেয়ার প্ল্যান: COPD রোগীর গ্যাস বিনিময়ে ব্যাঘাত",
            bodyEn = "Assessment: Dyspnea, barrel chest, SpO2 86% on room air, PaCO2 56 mmHg.\nDiagnosis: Impaired Gas Exchange r/t alveolar-capillary membrane changes.\nGoal: Maintain SpO2 88–92% and decreased work of breathing within 4 hours.\nInterventions & Rationales: (1) Venturi O2 24–28% -> Prevents blunting hypoxic respiratory drive; (2) Pursed-lip breathing -> Keeps small airways open during exhalation; (3) Nebulized bronchodilators -> Relaxes smooth muscle.\nEvaluation: SpO2 improved to 90%, RR decreased from 28 to 20/min.",
            bodyBn = "মূল্যায়ন: শ্বাসকষ্ট, SpO2 ৮৬%।\nডায়াগনোসিস: অ্যালভিওলার পরিবর্তনের কারণে গ্যাস বিনিময়ে ব্যাঘাত।\nলক্ষ্য: ৪ ঘণ্টার মধ্যে SpO2 ৮৮–৯২% বজায় রাখা।\nহস্তক্ষেপ: ভেনচুরি মাস্কে ২৪–২৮% অক্সিজেন, পার্সড-লিপ ব্রিদিং ও নেবুলাইজেশন।",
            sourceTitle = "GOLD COPD Strategy & Nursing Care Planning Guide",
            updatedDate = "2024-12-20"
        ),
        // Definitions
        StudyItemEntity(
            id = "def_1",
            itemType = "DEFINITION",
            subject = "Fundamentals of Nursing",
            titleEn = "Essential Clinical Terminology (A–Z Quick Glossary)",
            titleBn = "গুরুত্বপূর্ণ ক্লিনিক্যাল পরিভাষা (সংজ্ঞা)",
            bodyEn = "• Cyanosis: Bluish discoloration of skin/mucosa when deoxygenated Hb >5 g/dL.\n• Dyspnea: Subjective sensation of difficult or labored breathing.\n• Orthopnea: Shortness of breath when lying flat, relieved by sitting upright.\n• Nosocomial Infection (HAI): Infection acquired >=48h after hospital admission.\n• Tachypnea: Adult respiratory rate >20 breaths/min.",
            bodyBn = "• সায়ানোসিস: অক্সিজেনের অভাবে ত্বক বা ঠোঁট নীলচে হয়ে যাওয়া।\n• ডিসপনিয়া: শ্বাসকষ্টের অনুভূতি।\n• অর্থোপনিয়া: শুয়ে থাকলে শ্বাসকষ্ট হওয়া যা উঠে বসলে কমে।\n• নসোকোমিয়াল সংক্রমণ (HAI): হাসপাতালে ভর্তির ৪৮ ঘণ্টা পর হওয়া সংক্রমণ।",
            sourceTitle = "Taber's Cyclopedic Medical Dictionary Reference",
            updatedDate = "2024-10-10"
        ),
        // Career & Licensing Zone
        StudyItemEntity(
            id = "career_bnmc",
            itemType = "EXAM",
            subject = "Licensing Exam Prep",
            titleEn = "BNMC (Bangladesh) & International Licensing (NCLEX-RN / UK NMC CBT)",
            titleBn = "BNMC লাইসেন্সিং এবং আন্তর্জাতিক নার্সিং পরীক্ষা (NCLEX-RN / UK NMC)",
            bodyEn = "• BNMC Comprehensive Licensing Exam (Bangladesh): Covers Diploma & B.Sc. Nursing syllabus, clinical safety, midwifery, community health. Verify latest circular directly on bnmc.gov.bd.\n• NCLEX-RN (USA/Canada/Australia): Computerized Adaptive Testing (NGN Clinical Judgment Model — Recognize Cues, Analyze Cues, Prioritize Hypotheses, Generate Solutions, Take Action, Evaluate Outcomes).\n• UK NMC Registration: CBT + OSCE + OET/IELTS Academic.",
            bodyBn = "• BNMC লাইসেন্সিং পরীক্ষা: বাংলাদেশ নার্সিং ও মিডওয়াইফারি কাউন্সিলের সর্বশেষ বিজ্ঞপ্তি bnmc.gov.bd-এ যাচাই করুন।\n• NCLEX-RN (যুক্তরাষ্ট্র/কানাডা/অস্ট্রেলিয়া): Next Generation NCLEX ক্লিনিক্যাল জাজমেন্ট মডেল।\n• UK NMC (যুক্তরাজ্য): CBT, OSCE এবং OET/IELTS।",
            sourceTitle = "BNMC, NCSBN & UK NMC Official Portals",
            sourceUrl = "https://www.ncsbn.org/nclex.page",
            updatedDate = "2025-01-15"
        ),
        StudyItemEntity(
            id = "career_jobs_intl",
            itemType = "CAREER",
            subject = "Career & Jobs",
            titleEn = "Govt/Private Nursing Jobs, Interview Questions & International Pathways",
            titleBn = "সরকারি/বেসরকারি নার্সিং চাকরি, ইন্টারভিউ ও আন্তর্জাতিক ক্যারিয়ার",
            bodyEn = "• Govt Senior Staff Nurse Exam Prep (BPSC / State PSC): Focus on Med-Surg, Fundamentals, Midwifery, Community Health, Pharmacology & General Knowledge.\n• Top Interview Questions: (1) Tell us how you handle a critical medication error; (2) Prioritize 4 acute patients using ABCs; (3) Explain WHO infection control in your ward.\n• International Demand: UK, Germany, Japan, Singapore, Middle East (DHA/HAAD/MOH), Canada, USA — always verify official embassy/health ministry portals to avoid agency fraud.",
            bodyBn = "• সরকারি সিনিয়র স্টাফ নার্স প্রস্তুতি: মেডিকেল-সার্জিক্যাল, ফান্ডামেন্টালস, মিডওয়াইফারি ও ফার্মাকোলজি।\n• সাধারণ ইন্টারভিউ প্রশ্ন: রোগীর অগ্রাধিকার নির্ধারণ (ABC), ওষুধের ত্রুটি ব্যবস্থাপনা ও সংক্রমণ নিয়ন্ত্রণ।\n• বিদেশে ক্যারিয়ার: যুক্তরাজ্য, জার্মানি, জাপান, কানাডা, মধ্যপ্রাচ্য—সর্বদা সরকারি/অফিশিয়াল পোর্টালে তথ্য যাচাই করুন।",
            sourceTitle = "WHO State of the World's Nursing & Official Regulatory Portals",
            sourceUrl = "https://www.nmc.org.uk",
            updatedDate = "2025-01-18"
        ),
        StudyItemEntity(
            id = "career_scholarship",
            itemType = "SCHOLARSHIP",
            subject = "Scholarships",
            titleEn = "Nursing Scholarships & Higher Education (MPH / M.Sc. Nursing)",
            titleBn = "নার্সিং স্কলারশিপ ও উচ্চশিক্ষা (MPH / M.Sc. Nursing)",
            bodyEn = "• Chevening Scholarships (UK — Master's in Public Health / Advanced Nursing)\n• Commonwealth Shared Scholarships (Health & Nursing Leadership)\n• Fulbright Foreign Student Program (Public Health / Nursing Science)\n• MEXT Scholarship (Japan — Nursing & Healthcare Research)\nCheck eligibility, language test deadlines, and official government portals annually.",
            bodyBn = "• চেভেনিং স্কলারশিপ (যুক্তরাজ্য)\n• কমনওয়েলথ স্কলারশিপ\n• ফুলব্রাইট স্কলারশিপ (যুক্তরাষ্ট্র)\n• MEXT স্কলারশিপ (জাপান)\nপ্রতি বছর অফিশিয়াল ওয়েবসাইটে যোগ্যতা ও সময়সীমা যাচাই করুন।",
            sourceTitle = "Official Scholarship Portals (Chevening / Commonwealth / MEXT)",
            sourceUrl = "https://www.chevening.org",
            updatedDate = "2025-01-12"
        )
    )

    fun initialMcqs(): List<McqQuestionEntity> = listOf(
        McqQuestionEntity(
            id = "mcq_1",
            subject = "Medical-Surgical Nursing",
            difficulty = "NCLEX / Clinical",
            questionEn = "A patient receiving IV Magnesium Sulfate for severe pre-eclampsia has a respiratory rate of 10 breaths/min and absent patellar reflexes. What is the nurse's FIRST action?",
            questionBn = "সিভিয়ার প্রি-এক্লাম্পসিয়ার জন্য IV ম্যাগনেসিয়াম সালফেট চলাকালীন রোগীর শ্বাসের হার ১০/মিনিট এবং প্যাটেলার রিফ্লেক্স অনুপস্থিত। নার্সের প্রথম করণীয় কী?",
            optionA = "Increase the IV infusion rate",
            optionB = "Stop the Magnesium Sulfate infusion and prepare Calcium Gluconate",
            optionC = "Administer Furosemide 40 mg IV immediately",
            optionD = "Reassess vital signs in 1 hour",
            correctIndex = 1,
            rationaleEn = "RR < 12/min and absent DTRs indicate Magnesium Sulfate toxicity. Stop the infusion immediately and administer the antidote, 10% Calcium Gluconate IV.",
            rationaleBn = "শ্বাস ১২/মিনিটের কম এবং রিফ্লেক্স না থাকা MgSO4 বিষক্রিয়ার লক্ষণ। অবিলম্বে ইনফিউশন বন্ধ করে অ্যান্টিডোট ক্যালসিয়াম গ্লুকোনেট দিতে হবে।",
            sourceTitle = "WHO Eclampsia Protocol"
        ),
        McqQuestionEntity(
            id = "mcq_2",
            subject = "Pharmacology",
            difficulty = "High-Alert",
            questionEn = "Which laboratory value must the nurse verify BEFORE initiating a continuous IV Regular Insulin infusion for a patient in Diabetic Ketoacidosis (DKA)?",
            questionBn = "ডায়াবেটিক কিটোঅ্যাসিডোসিস (DKA) রোগীর IV রেগুলার ইনসুলিন শুরু করার আগে কোন ল্যাব রিপোর্ট অবশ্যই যাচাই করতে হবে?",
            optionA = "Serum Sodium",
            optionB = "Serum Potassium (K+)",
            optionC = "Serum Calcium",
            optionD = "Hemoglobin A1c",
            correctIndex = 1,
            rationaleEn = "Insulin shifts potassium from blood into cells. If K+ is < 3.3 mEq/L, giving IV insulin can trigger life-threatening hypokalemia and fatal cardiac arrhythmias.",
            rationaleBn = "ইনসুলিন পটাশিয়ামকে রক্ত থেকে কোষে পাঠায়। K+ ৩.৩ mEq/L-এর কম হলে মারাত্মক হাইপোক্যালেমিয়া ও হার্ট অ্যারেস্ট হতে পারে।",
            sourceTitle = "ADA DKA Management Standards"
        ),
        McqQuestionEntity(
            id = "mcq_3",
            subject = "Emergency & Trauma Nursing",
            difficulty = "Priority",
            questionEn = "A patient develops sudden stridor, facial swelling, and hypotension 5 minutes after IV Ceftriaxone. What is the FIRST-LINE medication and route?",
            questionBn = "IV সেফট্রায়াক্সোন দেওয়ার ৫ মিনিট পর রোগীর স্ট্রাইডর, মুখ ফোলা ও নিম্ন রক্তচাপ দেখা দিলে প্রথম সারির ওষুধ ও প্রয়োগের পথ কী?",
            optionA = "Diphenhydramine 50 mg PO",
            optionB = "Hydrocortisone 100 mg IV",
            optionC = "Epinephrine 1:1000 (0.3–0.5 mg) Intramuscular (IM) in the anterolateral thigh",
            optionD = "Salbutamol oral tablet",
            correctIndex = 2,
            rationaleEn = "IM Epinephrine 1:1000 in the vastus lateralis is the undisputed first-line emergency treatment for anaphylaxis; antihistamines and steroids are secondary.",
            rationaleBn = "অ্যানাফাইল্যাক্সিসে প্রথম ও তাৎক্ষণিক জীবনরক্ষাকারী চিকিৎসা হলো উরুর পেশীতে (IM) ১:১০০০ এপিনেফ্রিন।",
            sourceTitle = "World Allergy Organization Guidelines"
        ),
        McqQuestionEntity(
            id = "mcq_4",
            subject = "Fundamentals of Nursing",
            difficulty = "Standard",
            questionEn = "What is the gold-standard method to confirm initial Nasogastric (NG) tube placement before administering enteral nutrition or medications?",
            questionBn = "ফিডিং বা ওষুধ দেওয়ার আগে প্রথমবার স্থাপিত NG টিউবের অবস্থান নিশ্চিত করার গোল্ড-স্ট্যান্ডার্ড পদ্ধতি কোনটি?",
            optionA = "Auscultating air bolus over the epigastrium",
            optionB = "Radiographic (X-ray) confirmation",
            optionC = "Observing bubbles when placing tip in water",
            optionD = "Asking the patient to hum",
            correctIndex = 1,
            rationaleEn = "X-ray confirmation is the gold standard for initial NG placement verification; air-bolus auscultation is unreliable and no longer recommended.",
            rationaleBn = "প্রাথমিকভাবে NG টিউবের সঠিক অবস্থান নিশ্চিত করার নির্ভরযোগ্য গোল্ড-স্ট্যান্ডার্ড হলো এক্স-রে পরীক্ষা।",
            sourceTitle = "ASPEN Enteral Safety Guidelines"
        ),
        McqQuestionEntity(
            id = "mcq_5",
            subject = "Infection Prevention & Control",
            difficulty = "Standard",
            questionEn = "After caring for a patient with Clostridioides difficile (C. diff) diarrhea, how must the nurse perform hand hygiene?",
            questionBn = "ক্লসট্রিডিওইডিস ডিফিসিল (C. diff) ডায়রিয়ার রোগীর সেবার পর নার্স কীভাবে হাত পরিষ্কার করবেন?",
            optionA = "Alcohol-based hand rub for 15 seconds",
            optionB = "Wash hands thoroughly with soap and running water",
            optionC = "Wipe hands with a dry sterile towel",
            optionD = "Wear double gloves without washing",
            correctIndex = 1,
            rationaleEn = "Alcohol-based hand sanitizers do not kill C. difficile spores. Soap and running water mechanically removes spores from the hands.",
            rationaleBn = "অ্যালকোহল হ্যান্ড রাব C. diff-এর স্পোর ধ্বংস করতে পারে না; তাই সাবান ও প্রবহমান পানি দিয়ে হাত ধোয়া বাধ্যতামূলক।",
            sourceTitle = "CDC / WHO Hand Hygiene Guidelines"
        ),
        McqQuestionEntity(
            id = "mcq_6",
            subject = "Pediatric Nursing",
            difficulty = "Standard",
            questionEn = "Under WHO IMCI guidelines, what is the recommended Oral Rehydration Solution (ORS) volume for a child with Some Dehydration (Plan B) over 4 hours?",
            questionBn = "WHO IMCI অনুযায়ী 'কিছু পানিশূন্যতা' (Plan B) থাকা শিশুকে ৪ ঘণ্টায় কতটুকু ORS দিতে হবে?",
            optionA = "20 mL/kg",
            optionB = "75 mL/kg",
            optionC = "150 mL/kg",
            optionD = "10 mL/kg",
            correctIndex = 1,
            rationaleEn = "WHO Plan B recommends 75 mL/kg of low-osmolarity ORS given slowly over 4 hours, followed by reassessment.",
            rationaleBn = "WHO প্ল্যান B অনুযায়ী ৪ ঘণ্টায় প্রতি কেজি ওজনের জন্য ৭৫ মিলি হারে ORS খাওয়াতে হয়।",
            sourceTitle = "WHO IMCI Chart Booklet"
        ),
        McqQuestionEntity(
            id = "mcq_7",
            subject = "Maternal & Child Health Nursing",
            difficulty = "Priority",
            questionEn = "Following vaginal delivery, the nurse palpates a soft, boggy uterine fundus displaced above and to the right of the umbilicus. What should the nurse do FIRST?",
            questionBn = "প্রসবের পর জরায়ুর ফান্ডাস নরম (boggy) ও নাভির ডানদিকে সরে থাকলে নার্সের প্রথম কাজ কী?",
            optionA = "Document as normal involution",
            optionB = "Massage the fundus firmly while supporting the lower uterine segment and check for bladder distension",
            optionC = "Place the mother in Trendelenburg position",
            optionD = "Remove the IV line",
            correctIndex = 1,
            rationaleEn = "Uterine atony is the #1 cause of PPH; firm fundal massage promotes contraction, and a full bladder displaces the uterus and prevents contraction.",
            rationaleBn = "নরম জরায়ু PPH-এর প্রধান কারণ; তাই নিচের অংশ সাপোর্ট দিয়ে ফান্ডাল ম্যাসাজ করতে হবে এবং মূত্রথলি পূর্ণ কিনা দেখতে হবে।",
            sourceTitle = "WHO PPH Prevention & Management"
        ),
        McqQuestionEntity(
            id = "mcq_8",
            subject = "Anatomy & Physiology",
            difficulty = "Standard",
            questionEn = "Which structure normally acts as the primary physiological pacemaker of the human heart at 60–100 beats per minute?",
            questionBn = "মানব হৃৎপিণ্ডের স্বাভাবিক প্রাইমারি পেসমেকার কোনটি (৬০–১০০ বিট/মিনিট)?",
            optionA = "AV Node",
            optionB = "Purkinje Fibers",
            optionC = "Sinoatrial (SA) Node",
            optionD = "Bundle of His",
            correctIndex = 2,
            rationaleEn = "The SA node in the right atrium initiates impulses at 60–100 bpm; AV node fires at 40–60 bpm as backup.",
            rationaleBn = "ডান অলিন্দে অবস্থিত SA নোড হৃৎপিণ্ডের প্রাইমারি পেসমেকার হিসেবে ৬০–১০০ বিট/মিনিট সংকেত তৈরি করে।",
            sourceTitle = "Tortora Anatomy & Physiology"
        )
    )
}

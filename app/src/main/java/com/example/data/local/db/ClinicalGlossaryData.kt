package com.example.data.local.db

import com.example.data.local.AppLanguage
import com.example.data.local.bi

data class GlossaryTerm(
    val id: String,
    val termEn: String,
    val termBn: String,
    val aliases: List<String>,
    val category: String,
    val definitionEn: String,
    val definitionBn: String,
    val clinicalSignificanceEn: String,
    val clinicalSignificanceBn: String,
    val nursingActionEn: String,
    val nursingActionBn: String
)

object ClinicalGlossaryData {
    val terms: List<GlossaryTerm> = listOf(
        GlossaryTerm(
            id = "gl_nursing",
            termEn = "Nursing",
            termBn = "নার্সিং (Nursing)",
            aliases = listOf("nursing", "nurse", "নার্সিং", "নার্স"),
            category = "Fundamentals of Nursing",
            definitionEn = "WHO / ICN Definition: Nursing encompasses autonomous and collaborative care of individuals of all ages, families, groups, and communities, sick or well, in all settings. It includes health promotion, illness prevention, and care of ill, disabled, and dying people.\nVirginia Henderson: Assisting the individual, sick or well, in activities contributing to health, recovery, or peaceful death.",
            definitionBn = "WHO / ICN অনুযায়ী নার্সিং: সকল বয়সী সুস্থ বা অসুস্থ ব্যক্তি, পরিবার ও সমাজের জন্য স্বতন্ত্র ও সমন্বিত স্বাস্থ্যসেবা—যার মধ্যে স্বাস্থ্য উন্নয়ন, রোগ প্রতিরোধ এবং অসুস্থ, প্রতিবন্ধী ও মুমূর্ষু ব্যক্তির যত্ন অন্তর্ভুক্ত।",
            clinicalSignificanceEn = "Guided by the 5-step Nursing Process (ADPIE: Assessment, Diagnosis, Planning, Implementation, Evaluation) and 4 metaparadigms (Person, Environment, Health, Nursing).",
            clinicalSignificanceBn = "নার্সিং প্রক্রিয়া (ADPIE) ও ৪টি মূল স্তম্ভ (ব্যক্তি, পরিবেশ, স্বাস্থ্য, নার্সিং) দ্বারা পরিচালিত।",
            nursingActionEn = "Practice holistic, evidence-based, ethical, and compassionate patient-centered care.",
            nursingActionBn = "প্রমাণ-ভিত্তিক, নৈতিক ও সহানুভূতিশীল রোগী-কেন্দ্রিক সেবা প্রদান করুন।"
        ),
        GlossaryTerm(
            id = "gl_pain",
            termEn = "Pain",
            termBn = "ব্যথা (Pain)",
            aliases = listOf("pain", "ache", "ব্যথা", "পেইন", "যন্ত্রণা"),
            category = "Fundamentals of Nursing",
            definitionEn = "IASP (2020) Definition: An unpleasant sensory and emotional experience associated with, or resembling that associated with, actual or potential tissue damage.\nNursing Definition (Margo McCaffery): 'Pain is whatever the experiencing person says it is, existing whenever the experiencing person says it does.'",
            definitionBn = "IASP (২০২০): প্রকৃত বা সম্ভাব্য টিস্যু ক্ষতির সাথে সম্পর্কিত একটি অপ্রীতিকর সংবেদনশীল ও মানসিক অভিজ্ঞতা। নার্সিং সংজ্ঞা: ব্যথা হলো রোগী যা বলে তা-ই (৫ম ভাইটাল সাইন)।",
            clinicalSignificanceEn = "Classified as Acute vs Chronic, and Nociceptive (somatic/visceral) vs Neuropathic. Assessed using PQRST and Numeric/FACES pain scales.",
            clinicalSignificanceBn = "তীব্র বনাম দীর্ঘস্থায়ী এবং নোসিসেপ্টিভ বনাম নিউরোপ্যাথিক ব্যথা। PQRST ও ০–১০ স্কেলে মূল্যায়ন করা হয়।",
            nursingActionEn = "Assess PQRST, administer prescribed WHO-ladder analgesics, apply comfort measures, and reassess in 30–60 minutes.",
            nursingActionBn = "PQRST মূল্যায়ন করুন, নির্ধারিত ব্যথানাশক ও আরামদায়ক ব্যবস্থা নিন এবং ৩০–৬০ মিনিট পর পুনরায় যাচাই করুন।"
        ),
        GlossaryTerm(
            id = "gl_health",
            termEn = "Health",
            termBn = "স্বাস্থ্য (Health)",
            aliases = listOf("health", "স্বাস্থ্য"),
            category = "Fundamentals of Nursing",
            definitionEn = "WHO Definition (1948): Health is a state of complete physical, mental, and social well-being and not merely the absence of disease or infirmity.",
            definitionBn = "WHO (১৯৪৮) অনুযায়ী স্বাস্থ্য: শুধুমাত্র রোগ বা দুর্বলতার অনুপস্থিতি নয়, বরং শারীরিক, মানসিক ও সামাজিকভাবে সম্পূর্ণ সুস্থ থাকার অবস্থা।",
            clinicalSignificanceEn = "Forms the holistic goal of all nursing and public health interventions.",
            clinicalSignificanceBn = "সকল নার্সিং ও জনস্বাস্থ্য সেবার চূড়ান্ত লক্ষ্য।",
            nursingActionEn = "Assess physical, psychological, and social determinants—not just physical symptoms.",
            nursingActionBn = "শুধু শারীরিক নয়, রোগীর মানসিক ও সামাজিক দিকও মূল্যায়ন করুন।"
        ),
        GlossaryTerm(
            id = "gl_asepsis",
            termEn = "Asepsis (Medical & Surgical)",
            termBn = "অ্যাসেপসিস (জীবাণুমুক্ত পদ্ধতি)",
            aliases = listOf("asepsis", "sterile", "sterilization", "disinfection", "অ্যাসেপসিস", "জীবাণুমুক্ত"),
            category = "Infection Control",
            definitionEn = "Asepsis is the absence of disease-producing microorganisms. Medical Asepsis (clean technique) reduces pathogen numbers and spread (hand hygiene, gloves). Surgical Asepsis (sterile technique) eliminates ALL microorganisms including spores (used in catheterization, surgery, sterile dressing).",
            definitionBn = "রোগ সৃষ্টিকারী জীবাণুর অনুপস্থিতিকে অ্যাসেপসিস বলে। মেডিকেল অ্যাসেপসিস (Clean) জীবাণু কমায়, আর সার্জিক্যাল অ্যাসেপসিস (Sterile) স্পোরসহ সব জীবাণু ধ্বংস করে।",
            clinicalSignificanceEn = "Cornerstone of preventing Healthcare-Associated Infections (HAIs).",
            clinicalSignificanceBn = "হাসপাতালে সংক্রমণ (HAI) প্রতিরোধের প্রধান উপায়।",
            nursingActionEn = "Follow WHO 5 Moments of Hand Hygiene; never turn your back on or reach over a sterile field.",
            nursingActionBn = "হ্যান্ড হাইজিন মেনে চলুন এবং স্টেরাইল ফিল্ডের ওপরে হাত নেবেন না।"
        ),
        GlossaryTerm(
            id = "gl_tachycardia",
            termEn = "Tachycardia",
            termBn = "ট্যাকিকার্ডিয়া (দ্রুত হৃদস্পন্দন)",
            aliases = listOf("tachycardia", "fast heart rate", "ট্যাকিকার্ডিয়া"),
            category = "Cardiovascular / Vitals",
            definitionEn = "An abnormally rapid resting adult heart rate greater than 100 beats per minute (bpm).",
            definitionBn = "বিশ্রাম অবস্থায় প্রাপ্তবয়স্কদের হৃদস্পন্দন বা নাড়ির গতি প্রতি মিনিটে ১০০-এর বেশি হওয়া।",
            clinicalSignificanceEn = "Earliest warning sign of fever, hypovolemia/shock, hypoxia, pain, anxiety, anemia, or hyperthyroidism.",
            clinicalSignificanceBn = "জ্বর, শক, অক্সিজেন ঘাটতি, ব্যথা বা রক্তশূন্যতার প্রাথমিক লক্ষণ।",
            nursingActionEn = "Check BP, SpO2, temperature, pain, and ECG rhythm to identify the underlying cause.",
            nursingActionBn = "রক্তচাপ, SpO2, তাপমাত্রা, ব্যথা ও ইসিজি পরীক্ষা করে কারণ নির্ণয় করুন।"
        ),
        GlossaryTerm(
            id = "gl_bradycardia",
            termEn = "Bradycardia",
            termBn = "ব্র্যাডিকার্ডিয়া (ধীর হৃদস্পন্দন)",
            aliases = listOf("bradycardia", "slow heart rate", "ব্র্যাডিকার্ডিয়া"),
            category = "Cardiovascular / Vitals",
            definitionEn = "A resting adult heart rate slower than 60 beats per minute (bpm).",
            definitionBn = "বিশ্রাম অবস্থায় প্রাপ্তবয়স্কদের নাড়ির গতি প্রতি মিনিটে ৬০-এর কম হওয়া।",
            clinicalSignificanceEn = "Normal in trained athletes or sleep, but pathological in Digoxin toxicity, beta-blocker overdose, heart block, hypothyroidism, or increased ICP.",
            clinicalSignificanceBn = "খেলোয়াড়দের জন্য স্বাভাবিক হতে পারে, তবে ডিজক্সিন বিষক্রিয়া, বিটা-ব্লকার বা হার্ট ব্লকের কারণে বিপজ্জনক হতে পারে।",
            nursingActionEn = "Assess for dizziness, hypotension, or syncope; hold Digoxin/beta-blockers if HR < 60 bpm and notify clinician.",
            nursingActionBn = "মাথা ঘোরা বা নিম্ন রক্তচাপ দেখুন; HR < ৬০ হলে ডিজক্সিন/বিটা-ব্লকার দেওয়ার আগে চিকিৎসককে জানান।"
        ),
        GlossaryTerm(
            id = "gl_dyspnea",
            termEn = "Dyspnea",
            termBn = "ডিসপনিয়া (শ্বাসকষ্ট)",
            aliases = listOf("dyspnea", "shortness of breath", "breathlessness", "ডিসপনিয়া", "শ্বাসকষ্ট"),
            category = "Respiratory",
            definitionEn = "Subjective sensation of difficult, labored, or uncomfortable breathing ('air hunger').",
            definitionBn = "শ্বাস নিতে কষ্ট বা শ্বাসকষ্ট অনুভব করা।",
            clinicalSignificanceEn = "Seen in asthma, COPD, pneumonia, heart failure, pulmonary embolism, anemia, and anaphylaxis.",
            clinicalSignificanceBn = "অ্যাজমা, COPD, নিউমোনিয়া, হার্ট ফেইলিউর ও রক্তশূন্যতায় দেখা যায়।",
            nursingActionEn = "Position in High-Fowler's, check SpO2 and RR, auscultate lungs, and administer ordered oxygen.",
            nursingActionBn = "হাই-ফাউলার পজিশনে বসান, SpO2 দেখুন ও নির্দেশিত অক্সিজেন দিন।"
        ),
        GlossaryTerm(
            id = "gl_cyanosis",
            termEn = "Cyanosis",
            termBn = "সায়ানোসিস (নীলচে ভাব)",
            aliases = listOf("cyanosis", "blue skin", "সায়ানোসিস"),
            category = "Respiratory",
            definitionEn = "Bluish or purplish discoloration of the skin, lips, tongue, or nail beds caused by >= 5 g/dL of deoxygenated hemoglobin in the blood.",
            definitionBn = "রক্তে অক্সিজেনের তীব্র ঘাটতির কারণে ঠোঁট, জিহ্বা, ত্বক বা নখের গোড়া নীলচে হয়ে যাওয়া।",
            clinicalSignificanceEn = "Central cyanosis (tongue/oral mucosa) indicates systemic hypoxemia; peripheral cyanosis (fingers/toes) may reflect cold or poor peripheral perfusion.",
            clinicalSignificanceBn = "সেন্ট্রাল সায়ানোসিস (জিহ্বা/ঠোঁট) গুরুতর হাইপোক্সেমিয়ার লক্ষণ।",
            nursingActionEn = "Treat as urgent hypoxia: assess airway, check SpO2, apply supplemental oxygen, and alert physician.",
            nursingActionBn = "অবিলম্বে শ্বাসনালী ও SpO2 পরীক্ষা করে অক্সিজেন দিন।"
        ),
        GlossaryTerm(
            id = "gl_hypoxia",
            termEn = "Hypoxia & Hypoxemia",
            termBn = "হাইপোক্সিয়া ও হাইপোক্সেমিয়া",
            aliases = listOf("hypoxia", "hypoxemia", "হাইপোক্সিয়া", "হাইপোক্সেমিয়া"),
            category = "Respiratory",
            definitionEn = "Hypoxemia is abnormally low oxygen level in arterial blood (PaO2 < 80 mmHg or SpO2 < 94%). Hypoxia is inadequate oxygen delivery to body tissues.",
            definitionBn = "হাইপোক্সেমিয়া হলো রক্তে অক্সিজেন কমে যাওয়া, আর হাইপোক্সিয়া হলো শরীরের টিস্যুতে অক্সিজেনের ঘাটতি।",
            clinicalSignificanceEn = "Early signs: restlessness, anxiety, tachycardia, tachypnea. Late signs: cyanosis, bradycardia, confusion.",
            clinicalSignificanceBn = "প্রাথমিক লক্ষণ: অস্থিরতা, দ্রুত নাড়ি ও শ্বাস; দেরির লক্ষণ: সায়ানোসিস ও বিভ্রান্তি।",
            nursingActionEn = "Never wait for cyanosis—act on restlessness and falling SpO2 with airway positioning and oxygen.",
            nursingActionBn = "অস্থিরতা ও SpO2 কমলেই দ্রুত শ্বাসনালী খুলে অক্সিজেন দিন।"
        ),
        GlossaryTerm(
            id = "gl_edema",
            termEn = "Edema & Ascites",
            termBn = "এডিমা (শোথ) ও অ্যাসাইটিস",
            aliases = listOf("edema", "oedema", "ascites", "swelling", "এডিমা", "শোথ", "ফোলা"),
            category = "Fluid & Electrolytes",
            definitionEn = "Edema is abnormal accumulation of excess interstitial fluid in tissues (pitting grades 1+ to 4+). Ascites is fluid accumulation inside the peritoneal cavity.",
            definitionBn = "শরীরের টিস্যুতে অতিরিক্ত তরল জমে ফুলে যাওয়াকে এডিমা (শোথ) বলে এবং পেটের গহ্বরে পানি জমাকে অ্যাসাইটিস বলে।",
            clinicalSignificanceEn = "Common in heart failure, nephrotic syndrome/CKD, liver cirrhosis, and severe malnutrition (Kwashiorkor).",
            clinicalSignificanceBn = "হার্ট ফেইলিউর, কিডনি রোগ, লিভার সিরোসিস ও অপুষ্টিতে দেখা যায়।",
            nursingActionEn = "Track daily weight, measure abdominal girth (ascites), elevate edematous limbs, monitor diuretics & sodium intake.",
            nursingActionBn = "দৈনিক ওজন মাপুন, ফোলা অঙ্গ উঁচু রাখুন এবং ফ্লুইড ও লবণ নিয়ন্ত্রণে রাখুন।"
        ),
        GlossaryTerm(
            id = "gl_triage",
            termEn = "Triage",
            termBn = "ট্রায়াজ (Triage)",
            aliases = listOf("triage", "ট্রায়াজ"),
            category = "Emergency Nursing",
            definitionEn = "The systematic sorting and prioritization of patients according to the urgency of their clinical condition and need for immediate medical intervention (e.g., Red/Immediate, Yellow/Delayed, Green/Minor, Black/Expectant).",
            definitionBn = "রোগীর অবস্থার জরুরিতা অনুযায়ী চিকিৎসার অগ্রাধিকার নির্ধারণ ও শ্রেণিবিন্যাস করার পদ্ধতি (লাল=অবিলম্বে, হলুদ=বিলম্বিত, সবুজ=সাধারণ, কালো=মুমূর্ষু)।",
            clinicalSignificanceEn = "Maximizes survival in emergency departments and mass casualty incidents.",
            clinicalSignificanceBn = "জরুরি বিভাগ ও দুর্যোগে সর্বোচ্চ সংখ্যক জীবন বাঁচাতে সাহায্য করে।",
            nursingActionEn = "Always prioritize airway, breathing, and uncontrolled hemorrhage first.",
            nursingActionBn = "সর্বদা শ্বাসনালী, শ্বাস-প্রশ্বাস ও তীব্র রক্তপাতকে প্রথম অগ্রাধিকার দিন।"
        ),
        GlossaryTerm(
            id = "gl_empathy",
            termEn = "Empathy vs Sympathy",
            termBn = "এমপ্যাথি (সহমর্মিতা) বনাম সিমপ্যাথি",
            aliases = listOf("empathy", "sympathy", "therapeutic communication", "এমপ্যাথি", "সহমর্মিতা"),
            category = "Psychiatric & Fundamentals",
            definitionEn = "Empathy is the therapeutic ability to understand and share the patient's feelings objectively without losing professional boundaries ('I hear how frightening this diagnosis is for you'). Sympathy is feeling pity or sorrow for someone.",
            definitionBn = "এমপ্যাথি হলো পেশাগত সীমানা বজায় রেখে রোগীর অনুভূতি গভীরভাবে বোঝা ও তার পাশে থাকা; সিমপ্যাথি হলো কেবল করুণা বা দুঃখ প্রকাশ।",
            clinicalSignificanceEn = "Empathy builds therapeutic trust and reduces patient anxiety.",
            clinicalSignificanceBn = "এমপ্যাথি রোগীর সাথে থেরাপিউটিক আস্থা তৈরি করে।",
            nursingActionEn = "Use open-ended questions, active listening, and reflective statements.",
            nursingActionBn = "মনোযোগ দিয়ে শুনুন ও সহানুভূতিশীল উন্মুক্ত প্রশ্ন করুন।"
        ),
        GlossaryTerm(
            id = "gl_dehydration",
            termEn = "Dehydration",
            termBn = "ডিহাইড্রেশন (পানিশূন্যতা)",
            aliases = listOf("dehydration", "ডিহাইড্রেশন", "পানিশূন্যতা"),
            category = "Fluid & Pediatric Nursing",
            definitionEn = "A negative fluid balance caused by excessive loss of water and electrolytes (vomiting, diarrhea, hemorrhage, burns, polyuria) or inadequate intake.",
            definitionBn = "বমি, ডায়রিয়া, ঘাম বা অপর্যাপ্ত পানি পানের কারণে শরীরে পানি ও ইলেক্ট্রোলাইটের ঘাটতি।",
            clinicalSignificanceEn = "Signs: dry mucous membranes, poor skin turgor, sunken eyes/fontanelle, oliguria, tachycardia, hypotension.",
            clinicalSignificanceBn = "লক্ষণ: শুকনো মুখ, চোখ গর্তে ঢোকা, ত্বক টানলে দেরিতে ফেরা, প্রস্রাব কমে যাওয়া।",
            nursingActionEn = "Rehydrate with ORS (mild/moderate) or IV isotonic crystalloids (severe/shock) and track I&O.",
            nursingActionBn = "মৃদু/মাঝারি অবস্থায় ORS এবং তীব্র অবস্থায় IV স্যালাইন দিয়ে I&O মাপুন।"
        ),
        GlossaryTerm(
            id = "gl_hemorrhage",
            termEn = "Hemorrhage",
            termBn = "হেমোরেজ (রক্তক্ষরণ)",
            aliases = listOf("hemorrhage", "haemorrhage", "bleeding", "রক্তক্ষরণ", "রক্তপাত"),
            category = "Emergency Nursing",
            definitionEn = "Escape of blood from ruptured blood vessels, internally or externally (arterial = bright red spurting; venous = dark red steady flow; capillary = oozing).",
            definitionBn = "রক্তনালী ফেটে শরীরের ভেতরে বা বাইরে রক্ত বেরিয়ে যাওয়া।",
            clinicalSignificanceEn = "Rapid loss > 15–30% blood volume triggers hypovolemic shock.",
            clinicalSignificanceBn = "দ্রুত রক্তক্ষরণে হাইপোভোলেমিক শক হতে পারে।",
            nursingActionEn = "Apply direct firm pressure, elevate limb if appropriate, establish 2 large-bore IVs, monitor vitals.",
            nursingActionBn = "ক্ষতস্থানে সরাসরি চাপ দিন, ২টি বড় ক্যানুলা স্থাপন করুন ও ভাইটাল সাইন দেখুন।"
        ),
        GlossaryTerm(
            id = "gl_necrosis_ischemia",
            termEn = "Ischemia & Necrosis (Infarction)",
            termBn = "ইস্কেমিয়া ও নেক্রোসিস",
            aliases = listOf("ischemia", "ischaemia", "necrosis", "infarction", "ইস্কেমিয়া", "নেক্রোসিস"),
            category = "Pathology",
            definitionEn = "Ischemia is reversible inadequate blood supply to tissues causing oxygen deprivation. Necrosis (or Infarction) is irreversible cell and tissue death due to prolonged ischemia or injury.",
            definitionBn = "ইস্কেমিয়া হলো টিস্যুতে রক্ত ও অক্সিজেন সরবরাহ কমে যাওয়া (যা প্রাথমিক পর্যায়ে ফিরে আসতে পারে)। নেক্রোসিস/ইনফার্কশন হলো কোষের স্থায়ী মৃত্যু।",
            clinicalSignificanceEn = "Underlies Myocardial Infarction, Ischemic Stroke, and bowel strangulation.",
            clinicalSignificanceBn = "হার্ট অ্যাটাক ও স্ট্রোকের মূল কারণ।",
            nursingActionEn = "Monitor distal pulses (5 Ps: Pain, Pallor, Pulselessness, Paresthesia, Paralysis) and intervene early.",
            nursingActionBn = "নিয়মিত পেরিফেরাল পালস ও রঙ পরীক্ষা করে দ্রুত ব্যবস্থা নিন।"
        ),
        GlossaryTerm(
            id = "gl_urinary_terms",
            termEn = "Polyuria, Oliguria, Anuria, Dysuria & Hematuria",
            termBn = "প্রস্রাব সম্পর্কিত ক্লিনিক্যাল পরিভাষা",
            aliases = listOf("polyuria", "oliguria", "anuria", "dysuria", "hematuria", "পলিউরিয়া", "অলিগুরিয়া", "অ্যানুরিয়া", "ডিসুরিয়া", "হিমাচুরিয়া"),
            category = "Renal / Fundamentals",
            definitionEn = "• Polyuria: Excessive urine output (> 2.5–3 L/day) — seen in DM, DI.\n• Oliguria: Low urine output (< 400 mL/day or < 30 mL/hr) — shock, AKI.\n• Anuria: Absence or < 100 mL/day urine output.\n• Dysuria: Painful or burning urination — UTI.\n• Hematuria: Blood in urine — stones, trauma, glomerulonephritis.",
            definitionBn = "• পলিউরিয়া: অতিরিক্ত প্রস্রাব (> ২.৫–৩ লি/দিন)।\n• অলিগুরিয়া: কম প্রস্রাব (< ৪০০ mL/দিন বা < ৩০ mL/ঘণ্টা)।\n• অ্যানুরিয়া: প্রস্রাব না হওয়া (< ১০০ mL/দিন)।\n• ডিসুরিয়া: প্রস্রাবে জ্বালাপোড়া।\n• হিমাচুরিয়া: প্রস্রাবে রক্ত।",
            clinicalSignificanceEn = "Urine output < 30 mL/hr for 2 consecutive hours is an urgent warning of renal hypoperfusion or dehydration.",
            clinicalSignificanceBn = "টানা ২ ঘণ্টা < ৩০ mL/ঘণ্টা প্রস্রাব কিডনি বা রক্তসঞ্চালনের বিপদসংকেত।",
            nursingActionEn = "Maintain accurate Intake & Output (I&O) chart and notify clinician if output drops below 30 mL/hr.",
            nursingActionBn = "সঠিক I&O চার্ট রাখুন এবং < ৩০ mL/ঘণ্টা হলে দ্রুত জানান।"
        ),
        GlossaryTerm(
            id = "gl_gi_bleed_terms",
            termEn = "Hematemesis, Hemoptysis & Melena",
            termBn = "হিমাটেমেসিস, হিমোপটাইসিস ও মেলেনা",
            aliases = listOf("hematemesis", "hemoptysis", "melena", "হিমাটেমেসিস", "হিমোপটাইসিস", "মেলেনা"),
            category = "Medical-Surgical",
            definitionEn = "• Hematemesis: Vomiting of bright red blood or 'coffee-ground' material (upper GI bleed).\n• Hemoptysis: Coughing up blood from the respiratory tract (TB, bronchiectasis, PE).\n• Melena: Dark, tarry, foul-smelling stools from digested blood (upper GI bleed).",
            definitionBn = "• হিমাটেমেসিস: রক্তবমি (পাকস্থলী/খাদ্যনালীর রক্তপাত)।\n• হিমোপটাইসিস: কাশির সাথে রক্ত (ফুসফুস/শ্বাসনালী)।\n• মেলেনা: আলকাতরার মতো কালো দুর্গন্ধযুক্ত পায়খানা (উপরের পরিপাকতন্ত্রের রক্তপাত)।",
            clinicalSignificanceEn = "Crucial bedside differentiation between GI bleeding and respiratory bleeding.",
            clinicalSignificanceBn = "পরিপাকতন্ত্র বনাম শ্বাসতন্ত্রের রক্তপাত আলাদা করতে অত্যন্ত গুরুত্বপূর্ণ।",
            nursingActionEn = "Assess airway, vitals, Hb/Hct, and prepare IV fluids/blood type & crossmatch.",
            nursingActionBn = "শ্বাসনালী, রক্তচাপ ও হিমোগ্লোবিন পরীক্ষা করে জরুরি ব্যবস্থা নিন।"
        ),
        GlossaryTerm(
            id = "gl_tachypnea_bradypnea_apnea",
            termEn = "Tachypnea, Bradypnea & Apnea",
            termBn = "ট্যাকিপনিয়া, ব্র্যাডিপনিয়া ও অ্যাপনিয়া",
            aliases = listOf("tachypnea", "bradypnea", "apnea", "orthopnea", "ট্যাকিপনিয়া", "ব্র্যাডিপনিয়া", "অ্যাপনিয়া"),
            category = "Respiratory / Vitals",
            definitionEn = "• Tachypnea: Rapid shallow breathing (> 20 breaths/min in adults).\n• Bradypnea: Abnormally slow breathing (< 12 breaths/min in adults) — opioid toxicity.\n• Apnea: Temporary cessation of breathing.\n• Orthopnea: Shortness of breath when lying flat, relieved by sitting up (Heart Failure).",
            definitionBn = "• ট্যাকিপনিয়া: দ্রুত শ্বাস (> ২০/মিনিট)।\n• ব্র্যাডিপনিয়া: ধীর শ্বাস (< ১২/মিনিট — ওপিওয়েডে হয়)।\n• অ্যাপনিয়া: সাময়িকভাবে শ্বাস বন্ধ হওয়া।\n• অর্থোপনিয়া: শুয়ে থাকলে শ্বাসকষ্ট।",
            clinicalSignificanceEn = "Bradypnea (< 12/min) after Morphine/Fentanyl signals opioid overdose.",
            clinicalSignificanceBn = "মরফিন নেওয়ার পর শ্বাস < ১২/মিনিট হলে ওপিওয়েড বিষক্রিয়া বোঝায়।",
            nursingActionEn = "Count respirations for a full minute if irregular; hold opioids and prepare Naloxone if RR < 10–12/min.",
            nursingActionBn = "অস্বাভাবিক হলে পুরো ১ মিনিট শ্বাস গুনুন; ওপিওয়েডে শ্বাস কমে গেলে ন্যালোক্সন প্রস্তুত রাখুন।"
        ),
        GlossaryTerm(
            id = "gl_ethics_consent",
            termEn = "Nursing Ethics, Autonomy, Beneficence & Informed Consent",
            termBn = "নার্সিং নৈতিকতা ও ইনফর্মড কনসেন্ট",
            aliases = listOf("ethics", "autonomy", "beneficence", "nonmaleficence", "informed consent", "confidentiality", "নীতিশাস্ত্র", "সম্মতি"),
            category = "Nursing Ethics & Management",
            definitionEn = "• Autonomy: Patient's right to make their own healthcare decisions.\n• Beneficence: Acting in the best interest of the patient (doing good).\n• Non-maleficence: 'Do no harm'.\n• Justice: Fair and equal treatment.\n• Informed Consent: Voluntary permission after physician explains procedure, risks, benefits, and alternatives (Nurse witnesses signature & verifies comprehension).",
            definitionBn = "• অটোনমি: রোগীর নিজস্ব সিদ্ধান্তের অধিকার।\n• বেনিফিসেন্স: রোগীর মঙ্গল করা।\n• নন-ম্যালিফিসেন্স: ক্ষতি না করা।\n• জাস্টিস: ন্যায়বিচার।\n• ইনফর্মড কনসেন্ট: ঝুঁকি ও বিকল্প বুঝে রোগীর স্বেচ্ছা সম্মতি।",
            clinicalSignificanceEn = "Protects patient rights and legal/professional accountability.",
            clinicalSignificanceBn = "রোগীর অধিকার ও পেশাগত জবাবদিহিতা নিশ্চিত করে।",
            nursingActionEn = "Never coerce consent; if patient has questions about surgery risks, notify the surgeon before signing.",
            nursingActionBn = "চিকিৎসক ঝুঁকি বুঝিয়ে বলার আগে জোর করে সম্মতি নেবেন না।"
        ),
        GlossaryTerm(
            id = "gl_palliative",
            termEn = "Palliative Care & Hospice",
            termBn = "প্যালিয়েটিভ কেয়ার (উপশমকারী সেবা)",
            aliases = listOf("palliative", "hospice", "end of life", "প্যালিয়েটিভ"),
            category = "Fundamentals / Ethics",
            definitionEn = "WHO: Palliative care improves quality of life of patients and families facing life-threatening illness through prevention and relief of suffering (physical, psychosocial, spiritual). Can begin at diagnosis alongside curative care.",
            definitionBn = "দুরারোগ্য বা জীবন-হুমকিপূর্ণ রোগে রোগী ও পরিবারের শারীরিক, মানসিক ও আত্মিক কষ্ট লাঘব করে জীবনমান উন্নত করার সেবা।",
            clinicalSignificanceEn = "Focuses on symptom relief, dignity, comfort, and family support.",
            clinicalSignificanceBn = "ব্যথা ও কষ্ট কমানো এবং মর্যাদাপূর্ণ সেবা নিশ্চিত করা।",
            nursingActionEn = "Prioritize pain/dyspnea control, compassionate listening, and cultural/spiritual respect.",
            nursingActionBn = "ব্যথা ও শ্বাসকষ্ট উপশম এবং মানবিক সহানুভূতি বজায় রাখুন।"
        ),
        GlossaryTerm(
            id = "gl_homeostasis",
            termEn = "Homeostasis",
            termBn = "হোমিওস্ট্যাসিস (অভ্যন্তরীণ ভারসাম্য)",
            aliases = listOf("homeostasis", "হোমিওস্ট্যাসিস"),
            category = "Anatomy & Physiology",
            definitionEn = "The dynamic physiological process by which the body maintains a stable internal environment (temperature, pH 7.35–7.45, blood glucose, fluids, electrolytes) despite external changes.",
            definitionBn = "বাহ্যিক পরিবর্তনের মাঝেও শরীরের অভ্যন্তরীণ পরিবেশ (তাপমাত্রা, pH ৭.৩৫–৭.৪৫, গ্লুকোজ, তরল) স্থিতিশীল রাখার প্রক্রিয়া।",
            clinicalSignificanceEn = "Failure of homeostasis leads to shock, acidosis, hypothermia, or organ dysfunction.",
            clinicalSignificanceBn = "হোমিওস্ট্যাসিস নষ্ট হলে শক, অ্যাসিডোসিস বা অঙ্গ বিকল হতে পারে।",
            nursingActionEn = "Monitor vital signs, electrolytes, ABG, and fluid balance to support body compensation.",
            nursingActionBn = "ভাইটাল সাইন, ইলেক্ট্রোলাইট ও ফ্লুইড ব্যালেন্স পর্যবেক্ষণ করুন।"
        ),
        GlossaryTerm(
            id = "gl_pharmacology_rights",
            termEn = "Pharmacology & 10 Rights of Medication Administration",
            termBn = "ফার্মাকোলজি ও ওষুধ প্রয়োগের ১০টি অধিকার",
            aliases = listOf("pharmacology", "10 rights", "five rights", "medication rights", "ফার্মাকোলজি", "১০ অধিকার"),
            category = "Pharmacology",
            definitionEn = "Pharmacology is the study of drugs and their interactions with living systems (Pharmacokinetics: ADME — Absorption, Distribution, Metabolism, Excretion; Pharmacodynamics: drug action on body).\n10 Rights: Right Patient, Drug, Dose, Route, Time, Documentation, Reason, Assessment, Evaluation, Right to Refuse/Education.",
            definitionBn = "ফার্মাকোলজি হলো ওষুধ ও শরীরের ওপর তার ক্রিয়া-প্রতিক্রিয়ার বিজ্ঞান। ১০ অধিকার: সঠিক রোগী, ওষুধ, ডোজ, পথ, সময়, নথি, কারণ, মূল্যায়ন, ফলাফল ও প্রত্যাখ্যানের অধিকার।",
            clinicalSignificanceEn = "Prevents fatal medication administration errors.",
            clinicalSignificanceBn = "মারাত্মক ওষুধজনিত ভুল প্রতিরোধ করে।",
            nursingActionEn = "Always verify 2 patient identifiers and check allergies before administering any drug.",
            nursingActionBn = "যেকোনো ওষুধ দেওয়ার আগে রোগীর ২টি পরিচয় ও অ্যালার্জি যাচাই করুন।"
        )
    )

    private val stopWords = setOf(
        "definition", "define", "meaning", "what", "is", "are", "was", "were", "the",
        "of", "for", "in", "on", "to", "and", "or", "a", "an", "explain", "describe",
        "tell", "me", "about", "mean", "called", "term", "short", "note", "write",
        "কাকে", "বলে", "কী", "কি", "সম্পর্কে", "ব্যাখ্যা", "করো", "বলো", "সংজ্ঞা", "লিখো", "জানাও"
    )

    fun extractMeaningfulTokens(rawQuery: String): List<String> {
        return rawQuery.lowercase()
            .replace(Regex("[^a-z0-9\\u0980-\\u09FF\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 2 && it !in stopWords }
    }

    fun isDefinitionIntent(rawQuery: String): Boolean {
        val q = rawQuery.lowercase()
        return q.contains("definition") || q.contains("define ") ||
            q.contains("what is") || q.contains("meaning of") ||
            q.contains("কাকে বলে") || q.contains("সংজ্ঞা") || q.contains("মানে কি") || q.contains("মানে কী")
    }

    fun findBestMatch(rawQuery: String, meaningfulTokens: List<String>): Pair<GlossaryTerm, Int>? {
        val q = rawQuery.lowercase().trim()
        val isDef = isDefinitionIntent(q)

        return terms.map { term ->
            var score = 0
            for (alias in term.aliases) {
                val a = alias.lowercase()
                if (q == a || q == "definition of $a" || q == "define $a" || q == "what is $a" || q == "$a কাকে বলে") {
                    score += 30
                } else if (meaningfulTokens.any { it == a } || q.split(Regex("\\s+")).contains(a)) {
                    score += if (isDef) 16 else 12
                } else if (a.contains(" ") && q.contains(a)) {
                    score += 18
                }
            }
            for (tok in meaningfulTokens) {
                if (term.termEn.lowercase().split(Regex("[^a-z0-9]+")).contains(tok) ||
                    term.termBn.lowercase().contains(tok)
                ) {
                    score += 10
                }
            }
            term to score
        }.filter { it.second >= 10 }.maxByOrNull { it.second }
    }

    fun formatTermTitle(term: GlossaryTerm, lang: AppLanguage): String =
        bi(lang, "${term.termEn} — Nursing Definition", "${term.termBn} — নার্সিং সংজ্ঞা")
}

# NURSING AI — Smart Nursing Education & Clinical Assistant

**NURSING AI** is a modern, bilingual (**English & বাংলা**) Android application built with **Kotlin** and **Jetpack Compose (Material 3)** for nursing students, registered nurses, nursing educators, and healthcare professionals.

> **Clinical Safety Notice:** Nursing AI is an educational and clinical reference platform. It does not provide patient-specific medical diagnoses or replace independent clinical verification, licensed clinicians, or institutional hospital protocols.

---

## Key Features

### 1. ChatGPT-Style Universal Nursing Search Homepage
- Prominent *"Ask anything about Nursing..."* search and multi-turn conversation interface.
- **Voice Search** (`RECORD_AUDIO` + Android `SpeechRecognizer` supporting English and Bengali).
- **Image Search** via zero-permission Android Photo Picker (`PickVisualMedia`) for textbook pages and study notes.
- Dynamically organized nursing answers (Definition, Introduction, Causes/Risk Factors, Signs & Symptoms, Pathophysiology, Assessment, Investigations, Medical Management, Nursing Management, Interventions & Rationales, Complications, Prevention, Patient Education, Important Nursing Notes, and Verified References).
- **New Chat**, **Chat History**, **Recent Searches**, **Suggested Questions**, and **Save / Copy / Share** actions.

### 2. AI Nursing Assistant & Care Plans
- **AI Nursing Care Plan Generator** referencing NANDA-I, NIC, and NOC educational frameworks.
- **Smart AI Tools**: AI Nursing Tutor, Case Study Explainer, Bilingual Medical Term Translator (`EN ↔ বাংলা`), Note Summarizer, AI MCQ Generator, AI Viva Practice, Assignment Assistant, and Clinical Scenario Practice.
- **Visual Medical Learning**: Pre-bundled educational diagrams for Cardiac Anatomy, Respiratory System, and Peripheral IV Cannulation.
- *(Note: Live Gemini model calls in AI Studio Android apps require Firebase AI Logic; without Firebase provisioned, `NursingAiRepository` serves locally grounded references and clearly labeled sample outputs.)*

### 3. Nursing Study Hub & Clinical Library
- **Student Zone**: Personal Study Dashboard, Daily Study Goal, Study Streak, Weak Topic Detection, Interactive MCQs, and Spaced-Repetition Flashcards.
- **Notes & Q&A**: 17 nursing subjects covering Notes, Textbooks/WHO guides, Short/Broad Q&A, Viva, Case Studies, Assignments, Care Plans, NANDA-I Diagnoses, and Clinical Definitions.
- **Drug & Pharmacology Database**: Searchable monographs with generic/brand names, class, MOA, dosage, route, contraindications, side effects, ADRs, nursing responsibilities, interactions, counseling, storage, High-Alert flags, and source verification dates.
- **Nursing Procedure Library**: 12 core procedures (Hand Washing, Vital Signs, Injections, IV Cannulation, Catheterization, Wound Dressing, Oxygen Therapy, Suctioning, NG Tube Care, Bed Making, CPR/BLS, 10 Rights of Medication) with supervision notices.

### 4. Clinical Nursing Tools & Calculators
- **IV Flow Rate (`mL/hr` & `gtt/min`)** & **IV Drip Completion Time**
- **Medication Dosage** & **Pediatric Weight-Based Dose Support** (with safety cap & double-check warnings)
- **BMI**, **BSA (Mosteller)**, **24h Intake & Output Fluid Balance**
- **Glasgow Coma Scale (GCS)** & **APGAR Score**
- **Temperature (`°C ↔ °F ↔ K`)** & **Weight (`kg ↔ lb`)** Converters
- **Vital Signs Reference** & **SBAR Clinical Handover Template**

### 5. Profile, Career Zone & Admin Portal
- Language switcher (`English` / `বাংলা`) and Theme switcher (`Light` / `Dark` / `System`).
- Offline downloaded content search and saved AI answers.
- **Career & Licensing Zone**: BNMC, NCLEX-RN, UK NMC prep, govt/private job guides, scholarships, and an interactive **Nursing CV & Resume Builder**.
- **Admin Portal** (Demo PIN: `2026`): Manage Knowledge Sources, Drug Monographs, QA Feedback, and Reviewer Audit Logs.

---

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose, Material Design 3, Adaptive `NavigationBar` / `NavigationRail`, Edge-to-Edge (`WindowInsets`)
- **Architecture:** MVVM + Repository Pattern (`ViewModel`, `StateFlow`, Coroutines & Flow)
- **Local Persistence:** Room Database (KSP) + Jetpack DataStore Preferences
- **Media:** Coil Compose + Android Photo Picker
- **Testing:** JUnit 4 + Robolectric (`sdk = 36`)

---

## Build & Run

```bash
# Compile debug APK
./gradlew assembleDebug

# Run unit & Robolectric tests
./gradlew testDebugUnitTest
```

*(In AI Studio cloud builds, use `gradle assembleDebug` and `gradle :app:testDebugUnitTest`.)*

---

## Project Documentation
- See [ARCHITECTURE.md](ARCHITECTURE.md) for the complete system architecture, screen map, Room schema, and development roadmap.
- See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution and clinical content verification standards.

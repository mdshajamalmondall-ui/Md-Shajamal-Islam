# NURSING AI — Smart Nursing Education & Clinical Assistant

**NURSING AI** is a modern, bilingual (**English & বাংলা**) Android application built with **Kotlin** and **Jetpack Compose (Material 3)** for nursing students, registered nurses, nursing educators, and healthcare professionals.

> **Clinical Safety Notice:** Nursing AI is an educational and clinical reference platform. It does not provide patient-specific medical diagnoses or replace independent clinical verification, licensed clinicians, or institutional hospital protocols.

---

## Key Features

### 1. Search — ChatGPT-Style Universal Nursing Search (Live Gemini AI)
- Minimalist ChatGPT-style interface powered by **Firebase AI Logic (`gemini-3.8-flash`)** with Firebase App Check attestation, plus an **Offline Grounded Reference** fallback mode.
- Answers **any nursing, medical, pharmacology, procedure, or clinical question** (definitions, management, complications, indications, contraindications, drugs, short or detailed) in **English & বাংলা**.
- **Voice Search** (`RECORD_AUDIO` + Android `SpeechRecognizer` supporting `en-US` and `bn-BD`) and **Multimodal Image Search** via zero-permission Android Photo Picker (`PickVisualMedia`).
- Structured answers with Definition, Pathophysiology, Assessment, Medical & Nursing Management, Interventions & Rationales, Complications, Prevention, Patient Education, Nursing Notes, Citations, and Save/Copy/Share actions.

### 2. Tools (`টুলস`) — Clinical Calculators, AI & Study Suite
- **12 Clinical Calculators**: IV Flow Rate, IV Drip Time, Medication Dosage, Pediatric Dose, BMI, BSA, 24h I&O Fluid Balance, GCS, APGAR, Temperature & Weight Converters, and Vitals & SBAR Reference.
- **Care Plan & Smart AI Tools**: Live Gemini-powered NANDA-I / NIC / NOC Care Plan Generator, 8 Smart AI Tools (Tutor, Case Explainer, EN↔বাংলা Translator, Summarizer, MCQ Generator, Viva Practice, Assignment Helper, Ward Scenario), and Visual Anatomy Diagrams.
- **Drug & Pharmacology Database**, **12 Nursing Procedures**, and **MCQ & Spaced-Repetition Flashcards**.

### 3. Files (`ফাইলস`) — Nursing File Vault & Study Library
- **Personal File Vault**: Upload and organize unlimited PDFs, documents, assignments, lecture notes, and study photos into folders via zero-permission Android Storage Access Framework (`OpenDocument`) and `PickVisualMedia`. Open, share, search, and manage files offline.
- **Notes & Books Library**: Pre-bundled study notes, textbooks, Short/Broad Q&A, Viva guides, Case Studies, and NANDA-I Care Plans across 17 nursing subjects.

### 4. Profile (`প্রোফাইল`) — Student Profile, Notes & Saved Questions
- **Student Profile**: Custom **Student Profile Picture** (Photo Picker), **Full Name**, **Gmail**, **Phone Number**, **Nursing College/Hospital**, **Batch/Year**, and Role (`Student` / `Nurse` / `Educator`).
- **My Notes**: Create, tag, edit, search, copy, and delete personal nursing study notes stored in Room.
- **Saved Questions**: Bookmark and revisit favorite Q&As, expand full formatted answers, or reopen them directly in Search.
- **Career & Admin**: BNMC/NCLEX/UK NMC guides, Nursing CV Builder, Language & Theme toggles (`Light`/`Dark`/`System`), and Reviewer Admin Portal (Demo PIN: `2026`).

---

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose, Material Design 3, Adaptive `NavigationBar` / `NavigationRail`, Edge-to-Edge (`WindowInsets`)
- **AI Engine:** Firebase AI Logic (`gemini-3.8-flash`) + Firebase App Check (Debug & reCAPTCHA Enterprise) with Structured JSON (`Schema.obj`), Streaming, and Multimodal Vision + Room Offline RAG Fallback
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

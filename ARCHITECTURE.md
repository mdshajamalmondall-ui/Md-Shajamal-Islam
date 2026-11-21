# NURSING AI — Architecture, Screen Map, Schema & Roadmap

## 1. Product Architecture

```text
+-------------------------------------------------------------------+
|                        Jetpack Compose UI                         |
|  HomeScreen | AiAssistantScreen | StudyHubScreen | ClinicalTools  |
|  ProfileScreen (Settings, Offline, Career/CV, Admin Portal)       |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                  NursingViewModel (StateFlow)                     |
+-------------------------------------------------------------------+
          |                       |                       |
          v                       v                       v
+-------------------+   +-------------------+   +-------------------+
| NursingAiRepository|  | NursingRepository |   | ClinicalCalculators|
| (Interface)       |   | (Room + Seed)     |   | (Pure Domain Math)|
|  -> LocalGrounded |   +-------------------+   +-------------------+
|     AiRepository  |             |
|  (RAG + Sample)   |             v
+-------------------+   +-------------------+   +-------------------+
                        |  NursingDatabase  |   | UserPreferences   |
                        |  (Room + KSP)     |   | (DataStore)       |
                        +-------------------+   +-------------------+
```

### AI Abstraction Strategy
All AI calls (Chat Q&A, Image study context, Care Plan Generator, and 8 Smart AI Tools) flow through the single `NursingAiRepository` interface:
- **Current Implementation (`LocalGroundedAiRepository`)**: Performs bilingual keyword/token RAG scoring against Room `knowledge_docs`, `drugs`, and `procedures`, applies dynamic section filtering, detects clinical emergency red flags, and displays a notice that live Gemini generation requires Firebase AI Logic.
- **Future Cloud Upgrade**: Swap in a `FirebaseAiLogicRepository` implementing `NursingAiRepository` without changing UI or ViewModel contracts.

---

## 2. Screen Map & Navigation

Adaptive shell switches automatically between `NavigationBar` (`< 600.dp`) and `NavigationRail` (`>= 600.dp`):

1. **`home` (`HomeScreen`)**
   - ChatGPT-style Universal Nursing Search Homepage
   - Voice Search (`RECORD_AUDIO`), Photo Picker Image Search, `EN | বাংলা` toggle
   - Multi-turn chat stream, New Chat, Chat History modal bottom sheet
   - Recent searches, suggested questions, quick clinical tools, daily quiz, saved answers
2. **`ai_assistant` (`AiAssistantScreen`)**
   - Tab 1: Care Plan AI (NANDA-I / NIC / NOC)
   - Tab 2: Smart Tools (Tutor, Case Explainer, Translator, Summarizer, MCQ Gen, Viva, Assignment, Clinical Scenario)
   - Tab 3: Visual Learning Gallery (Heart, Respiratory, IV Cannulation diagrams)
3. **`study` (`StudyHubScreen`)**
   - Tab 1: Student Zone (Study Dashboard, Streak, Weak Topic Detection, Mock MCQ & Spaced Flashcards)
   - Tab 2: Notes & Q&A (17 subjects, filterable by type, bookmarks, offline save)
   - Tab 3: Drug Database (Monographs, High-Alert badges, source/verification date)
   - Tab 4: Procedures Library (12 core procedures with step-by-step & infection control checklists)
4. **`clinical_tools` (`ClinicalToolsScreen`)**
   - 12 interactive Clinical Calculators & Reference Charts (IV Flow Rate, Dosage, Pediatric Dose, BMI, BSA, IV Drip Time, I&O Fluid Balance, GCS, APGAR, Temp, Weight, Vitals & SBAR)
5. **`profile` (`ProfileScreen`)**
   - Tab 1: Settings (Profile, Role, Language `EN/বাংলা`, Theme `Light/Dark/System`)
   - Tab 2: Offline & Saved Library (Unified offline search across saved answers, drugs, procedures, notes)
   - Tab 3: Career & CV (BNMC, NCLEX-RN, UK NMC, Jobs, Scholarships, Nursing CV Builder)
   - Tab 4: Admin Portal (PIN-gated management for Knowledge Sources, Drugs, Feedback & Audit Logs)

---

## 3. Room Database Schema (`nursing_ai_clinical.db`)

| Table | Purpose | Key Fields |
|---|---|---|
| `chat_threads` | Multi-turn conversation threads | `id`, `title`, `languageCode`, `updatedAt`, `isPinned` |
| `chat_messages` | User & structured AI messages | `id`, `threadId` (FK), `role`, `content`, `sectionsSerialized`, `citationsSerialized`, `emergencyWarning`, `imageUri` |
| `search_history` | Recent search queries | `id`, `query`, `category`, `timestamp` |
| `saved_answers` | Bookmarked Q&A entries | `id`, `question`, `answerSummary`, `fullFormattedContent`, `citationsSerialized` |
| `knowledge_docs` | Decoupled RAG nursing knowledge base | `id`, `subject`, `titleEn/Bn`, `definition..nursingNotes (En/Bn)`, `sourceTitle`, `sourceUrl`, `publishedDate`, `verificationStatus`, `version` |
| `drugs` | Pharmacology reference database | `id`, `genericName`, `brandNames`, `classification`, `dosageReference`, `nursingResponsibilities`, `isHighAlert`, `isOfflineSaved` |
| `procedures` | Step-by-step clinical skills | `id`, `titleEn/Bn`, `supervisedTrainingRequired`, `equipment`, `steps`, `infectionControl`, `isOfflineSaved` |
| `study_items` | Notes, Q&A, Viva, Care Plans, Career | `id`, `itemType`, `subject`, `titleEn/Bn`, `bodyEn/Bn`, `isBookmarked`, `isOfflineSaved` |
| `mcq_questions` | Quiz & Spaced-Repetition bank | `id`, `subject`, `questionEn/Bn`, `optionA..D`, `correctIndex`, `rationaleEn/Bn`, `masteryBox`, `attempts`, `correctCount` |
| `audit_logs` | Admin content review & QA log | `id`, `actorRole`, `action`, `entityType`, `entityTitle`, `notes`, `timestamp` |

---

## 4. Development Roadmap
- **Phase 1 (Implemented):** Offline-first Room knowledge engine, ChatGPT-style homepage, voice/photo search, 12 clinical calculators, drug & procedure databases, study hub, career/CV builder, and local admin audit portal.
- **Phase 2 (Optional Cloud Sync):** Connect Firebase AI Logic (`gemini-3.8-flash`) behind `NursingAiRepository`, Firebase Auth (Google Sign-In via Credential Manager), and Firestore sync for multi-device study progress and editorial publishing.
- **Phase 3 (Institutional Integration):** Expand Bengali/English curriculum packs for BNMC Diploma, B.Sc., and Post-Basic B.Sc. Nursing syllabi with peer-reviewed clinical educator sign-off.

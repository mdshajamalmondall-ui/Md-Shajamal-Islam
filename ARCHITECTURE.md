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
| -> GeminiNursing  |   +-------------------+   +-------------------+
|    AiRepository   |             |
| (gemini-3.8-flash)|             v
| -> LocalGrounded  |   +-------------------+   +-------------------+
|    Fallback       |   |  NursingDatabase  |   | UserPreferences   |
+-------------------+   |  (Room + KSP)     |   | (DataStore)       |
                        +-------------------+   +-------------------+
```

### AI Abstraction Strategy
All AI calls (Chat Q&A, Multimodal Vision, Care Plan Generator, and 8 Smart AI Tools) flow through the single `NursingAiRepository` interface:
- **Primary Live Engine (`GeminiNursingAiRepository`)**: Powered by **Firebase AI Logic (`gemini-3.8-flash`)** and **Firebase App Check** (`AppCheckInit.kt`), featuring structured JSON schema enforcement (`Schema.obj`), streaming responses, multi-turn history, bilingual EN/বাংলা output, and multimodal image analysis.
- **Offline Reference Fallback (`LocalGroundedAiRepository`)**: Bilingual keyword/token RAG scoring against Room `knowledge_docs`, `drugs`, `procedures`, and clinical glossary when the user explicitly selects offline fallback.

---

## 2. Screen Map & Navigation

Adaptive shell switches automatically between `NavigationBar` (`< 600.dp`) and `NavigationRail` (`>= 600.dp`) with **4 main tabs**:

1. **`home` (`HomeScreen` — Search / সার্চ)**
   - Minimalist ChatGPT-style Universal Nursing Search & Chat UI
   - Top bar with History sheet, `EN | বাংলা` toggle, and New Chat button
   - Voice Search (`RECORD_AUDIO`), Photo Picker Image Search (`PickVisualMedia`), and quick topic starters
   - Accurate bilingual RAG search across 20+ full monographs, 55+ clinical glossary definitions, drugs, procedures, and study notes
2. **`tools` (`ToolsHubScreen` — Tools / টুলস)**
   - Sub-tab 1: 12 Clinical Calculators & Vitals/SBAR Reference (`ClinicalToolsScreen`)
   - Sub-tab 2: Care Plan AI (NANDA-I/NIC/NOC), 8 Smart AI Tools & Visual Anatomy (`AiAssistantScreen`)
   - Sub-tab 3: Drug & Pharmacology Database
   - Sub-tab 4: Nursing Procedures Library
   - Sub-tab 5: Student Zone (MCQ & Spaced-Repetition Flashcards)
3. **`files` (`FilesScreen` — Files / ফাইলস)**
   - Sub-tab 1: Personal File Vault (`UserFileEntity` via zero-permission `OpenDocument` SAF & `PickVisualMedia`) with folders, search, open, share, and delete
   - Sub-tab 2: Pre-bundled Nursing Notes, Books, Q&A, Viva & Case Studies across 17 subjects
4. **`profile` (`ProfileScreen` — Profile / প্রোফাইল)**
   - Student Header with Profile Picture picker (`PickVisualMedia`)
   - Sub-tab 1: Student Profile Info (Full Name, Gmail, Phone Number, Nursing College/Hospital, Batch, Role, Language & Theme)
   - Sub-tab 2: My Notes (`PersonalNoteEntity` CRUD with tags & search)
   - Sub-tab 3: Saved Questions (`SavedAnswerEntity` with expand, reopen in Search, copy & share)
   - Sub-tab 4: Career & CV Builder + PIN-gated Admin Portal (`2026`)

---

## 3. Room Database Schema (`nursing_ai_clinical.db`, v2)

| Table | Purpose | Key Fields |
|---|---|---|
| `chat_threads` | Multi-turn conversation threads | `id`, `title`, `languageCode`, `updatedAt`, `isPinned` |
| `chat_messages` | User & structured AI messages | `id`, `threadId` (FK), `role`, `content`, `sectionsSerialized`, `citationsSerialized`, `emergencyWarning`, `imageUri` |
| `search_history` | Recent search queries | `id`, `query`, `category`, `timestamp` |
| `saved_answers` | Bookmarked Q&A entries | `id`, `question`, `answerSummary`, `fullFormattedContent`, `citationsSerialized` |
| `personal_notes` | User-authored nursing notes | `id`, `title`, `content`, `tag`, `updatedAt` |
| `user_files` | Personal File Vault metadata | `id`, `title`, `folder`, `uriString`, `mimeType`, `sizeBytes`, `notes`, `addedAt` |
| `knowledge_docs` | Decoupled RAG nursing knowledge base | `id`, `subject`, `titleEn/Bn`, `definition..nursingNotes (En/Bn)`, `sourceTitle`, `sourceUrl`, `publishedDate`, `verificationStatus`, `version` |
| `drugs` | Pharmacology reference database | `id`, `genericName`, `brandNames`, `classification`, `dosageReference`, `nursingResponsibilities`, `isHighAlert`, `isOfflineSaved` |
| `procedures` | Step-by-step clinical skills | `id`, `titleEn/Bn`, `supervisedTrainingRequired`, `equipment`, `steps`, `infectionControl`, `isOfflineSaved` |
| `study_items` | Notes, Q&A, Viva, Care Plans, Career | `id`, `itemType`, `subject`, `titleEn/Bn`, `bodyEn/Bn`, `isBookmarked`, `isOfflineSaved` |
| `mcq_questions` | Quiz & Spaced-Repetition bank | `id`, `subject`, `questionEn/Bn`, `optionA..D`, `correctIndex`, `rationaleEn/Bn`, `masteryBox`, `attempts`, `correctCount` |
| `audit_logs` | Admin content review & QA log | `id`, `actorRole`, `action`, `entityType`, `entityTitle`, `notes`, `timestamp` |

---

## 4. Development Roadmap
- **Phase 1 (Implemented):** Offline-first Room knowledge engine, ChatGPT-style homepage, voice/photo search, 12 clinical calculators, drug & procedure databases, personal file vault, notes, student profile, career/CV builder, and local admin audit portal.
- **Phase 2 (Implemented):** Live **Firebase AI Logic (`gemini-3.8-flash`)** + **Firebase App Check** integration behind `NursingAiRepository` for open-ended bilingual nursing Q&A, drug/contraindication queries, NANDA-I care plans, smart tools, and multimodal image search.
- **Phase 3 (Optional Future Cloud Sync):** Optional Firebase Auth (Google Sign-In via Credential Manager) and Firestore sync for multi-device study progress and editorial publishing.

package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.LocalGroundedAiRepository
import com.example.data.local.AppLanguage
import com.example.data.local.NursingRepository
import com.example.data.local.db.NursingDatabase
import com.example.data.local.db.PersonalNoteEntity
import com.example.data.local.db.UserFileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NursingSearchAndStorageTest {

    private lateinit var db: NursingDatabase
    private lateinit var repo: NursingRepository
    private lateinit var aiRepo: LocalGroundedAiRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NursingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = NursingRepository(db)
        aiRepo = LocalGroundedAiRepository(db)
        runBlocking { repo.ensureSeeded() }
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `definition of pain returns pain answer not pneumonia`() = runBlocking {
        val res = aiRepo.askNursingQuestion("definition of pain", emptyList(), AppLanguage.ENGLISH)
        assertTrue("Expected Pain in summary, got: ${res.summary}", res.summary.contains("Pain", ignoreCase = true))
        assertFalse("Must not return Pneumonia", res.summary.contains("Pneumonia", ignoreCase = true))
        assertTrue(res.sections.first().body.contains("IASP", ignoreCase = true))
    }

    @Test
    fun `definition of nursing returns nursing definition not pneumonia`() = runBlocking {
        val res = aiRepo.askNursingQuestion("definition of nursing", emptyList(), AppLanguage.ENGLISH)
        assertTrue("Expected Nursing in summary, got: ${res.summary}", res.summary.contains("Nursing", ignoreCase = true))
        assertFalse("Must not return Pneumonia", res.summary.contains("Pneumonia", ignoreCase = true))
        assertTrue(res.sections.first().body.contains("WHO", ignoreCase = true))
    }

    @Test
    fun `bengali nursing definition query returns bengali nursing answer`() = runBlocking {
        val res = aiRepo.askNursingQuestion("নার্সিং কাকে বলে?", emptyList(), AppLanguage.BENGALI)
        assertTrue("Expected নার্সিং in summary, got: ${res.summary}", res.summary.contains("নার্সিং"))
        assertFalse("Must not return নিউমোনিয়া", res.summary.contains("নিউমোনিয়া"))
    }

    @Test
    fun `glossary definition query matches cyanosis accurately`() = runBlocking {
        val res = aiRepo.askNursingQuestion("what is cyanosis", emptyList(), AppLanguage.ENGLISH)
        assertTrue("Expected Cyanosis in summary, got: ${res.summary}", res.summary.contains("Cyanosis", ignoreCase = true))
    }

    @Test
    fun `pneumonia query still matches pneumonia`() = runBlocking {
        val res = aiRepo.askNursingQuestion("pneumonia management", emptyList(), AppLanguage.ENGLISH)
        assertTrue("Expected Pneumonia in summary, got: ${res.summary}", res.summary.contains("Pneumonia", ignoreCase = true))
    }

    @Test
    fun `unmatched query returns study framework instead of false pneumonia match`() = runBlocking {
        val res = aiRepo.askNursingQuestion("xyzunknownclinicaltopic999", emptyList(), AppLanguage.ENGLISH)
        assertTrue(res.summary.contains("Framework", ignoreCase = true))
        assertFalse(res.summary.contains("Pneumonia", ignoreCase = true))
    }

    @Test
    fun `personal notes and user files persist via repository`() = runBlocking {
        repo.upsertPersonalNote(PersonalNoteEntity(id = "n1", title = "Pain Note", content = "PQRST assessment", tag = "Lecture Note"))
        repo.upsertUserFile(UserFileEntity(id = "f1", title = "Anatomy.pdf", folder = "PDF Books", uriString = "content://test/1", mimeType = "application/pdf", sizeBytes = 2048))

        val notes = repo.personalNotesFlow.first()
        val files = repo.userFilesFlow.first()
        assertTrue(notes.any { it.id == "n1" && it.title == "Pain Note" })
        assertTrue(files.any { it.id == "f1" && it.title == "Anatomy.pdf" })
    }

    @Test
    fun `gemini structured JSON parser extracts drug and nursing sections cleanly`() {
        val sampleJson = """
            {
              "summary": "Paracetamol (Acetaminophen) — Analgesic & Antipyretic",
              "sections": [
                {"title": "Indications", "body": "Mild-to-moderate pain and fever management."},
                {"title": "Contraindications", "body": "Severe hepatic impairment or hypersensitivity."}
              ],
              "citations": [
                {"title": "BNF & WHO Essential Medicines", "url": "https://www.who.int", "publishedDate": "2025", "verificationStatus": "Verified Reference"}
              ],
              "relatedTopics": ["Paracetamol max daily dose", "Hepatotoxicity antidote (NAC)"],
              "emergencyWarning": ""
            }
        """.trimIndent()

        val parsed = com.example.data.ai.GeminiNursingAiRepository.parseStructuredResponse(
            sampleJson,
            AppLanguage.ENGLISH
        )
        assertTrue(parsed.isLiveGemini)
        assertTrue(parsed.summary.contains("Paracetamol"))
        assertTrue(parsed.sections.size == 2)
        assertTrue(parsed.sections[1].title == "Contraindications")
        assertTrue(parsed.relatedTopics.size == 2)
    }

    @Test
    fun `gemini structured JSON parser handles bengali emergency output`() {
        val sampleBnJson = """
            {
              "summary": "ব্যথার সংজ্ঞা ও নার্সিং মূল্যায়ন",
              "sections": [
                {"title": "১. সংজ্ঞা", "body": "IASP অনুযায়ী ব্যথা হলো একটি অপ্রীতিকর সংবেদনশীল ও মানসিক অভিজ্ঞতা।"}
              ],
              "emergencyWarning": "তীব্র বুকে ব্যথা হলে জরুরি প্রোটোকল অনুসরণ করুন।"
            }
        """.trimIndent()

        val parsed = com.example.data.ai.GeminiNursingAiRepository.parseStructuredResponse(
            sampleBnJson,
            AppLanguage.BENGALI
        )
        assertTrue(parsed.isLiveGemini)
        assertTrue(parsed.summary.contains("ব্যথা"))
        assertTrue(parsed.citations.isNotEmpty())
        assertTrue(parsed.emergencyWarning.contains("জরুরি"))
    }
}

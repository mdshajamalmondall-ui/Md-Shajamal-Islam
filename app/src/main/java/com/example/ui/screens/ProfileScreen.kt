package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppLanguage
import com.example.data.local.ThemeMode
import com.example.data.local.UserRole
import com.example.data.local.bi
import com.example.data.local.db.KnowledgeSeedData
import com.example.ui.components.ClinicalSafetyBanner
import com.example.ui.components.FirebaseAiNoticeBanner
import com.example.ui.viewmodel.NursingViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: NursingViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = prefs.language
    val savedAnswers by viewModel.savedAnswers.collectAsStateWithLifecycle()
    val studyItems by viewModel.studyItems.collectAsStateWithLifecycle()
    val drugs by viewModel.drugs.collectAsStateWithLifecycle()
    val procedures by viewModel.procedures.collectAsStateWithLifecycle()
    val knowledgeDocs by viewModel.knowledgeDocs.collectAsStateWithLifecycle()
    val mcqs by viewModel.mcqs.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = bi(lang, "Profile, Career & Admin", "প্রোফাইল, ক্যারিয়ার ও অ্যাডমিন"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(bi(lang, "Settings", "সেটিংস")) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(bi(lang, "Offline & Saved", "অফলাইন ও সংরক্ষিত")) })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(bi(lang, "Career & CV", "ক্যারিয়ার ও সিভি")) })
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text(bi(lang, "Admin Portal", "অ্যাডমিন প্যানেল")) })
                }
            }
        }

        when (selectedTab) {
            0 -> SettingsProfileTab(prefs = prefs, viewModel = viewModel)
            1 -> OfflineAndSavedTab(
                lang = lang,
                savedAnswers = savedAnswers,
                offlineDrugs = drugs.filter { it.isOfflineSaved },
                offlineProcedures = procedures.filter { it.isOfflineSaved },
                offlineStudy = studyItems.filter { it.isOfflineSaved },
                onDeleteSaved = viewModel::deleteSavedAnswer
            )
            2 -> CareerAndCvTab(
                lang = lang,
                careerItems = studyItems.filter { it.itemType in listOf("CAREER", "EXAM", "SCHOLARSHIP") }
            )
            3 -> AdminPortalTab(
                lang = lang,
                viewModel = viewModel,
                docsCount = knowledgeDocs.size,
                drugsCount = drugs.size,
                mcqsCount = mcqs.size,
                auditLogs = auditLogs
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsProfileTab(
    prefs: com.example.data.local.UserPreferences,
    viewModel: NursingViewModel
) {
    val lang = prefs.language
    var nameInput by remember(prefs.userName) { mutableStateOf(prefs.userName) }

    LazyColumn(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(bi(lang, "Nursing Profile", "নার্সিং প্রোফাইল"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(bi(lang, "Display Name", "নাম")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(bi(lang, "Role", "পেশাগত ভূমিকা"), style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        UserRole.entries.forEach { role ->
                            FilterChip(
                                selected = prefs.userRole == role,
                                onClick = { viewModel.updateUserProfile(nameInput, role) },
                                label = { Text(bi(lang, role.labelEn, role.labelBn)) }
                            )
                        }
                    }
                    Button(onClick = { viewModel.updateUserProfile(nameInput, prefs.userRole) }) {
                        Text(bi(lang, "Save Profile", "প্রোফাইল সংরক্ষণ করুন"))
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(bi(lang, "Language & Appearance", "ভাষা ও থিম"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { l ->
                            FilterChip(
                                selected = prefs.language == l,
                                onClick = { viewModel.setLanguage(l) },
                                label = { Text(l.displayLabel) }
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = prefs.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(bi(lang, mode.labelEn, mode.labelBn)) },
                                modifier = Modifier.testTag("theme_chip_${mode.key}")
                            )
                        }
                    }
                }
            }
        }

        item { FirebaseAiNoticeBanner(language = lang) }
        item { ClinicalSafetyBanner(language = lang) }
    }
}

@Composable
private fun OfflineAndSavedTab(
    lang: AppLanguage,
    savedAnswers: List<com.example.data.local.db.SavedAnswerEntity>,
    offlineDrugs: List<com.example.data.local.db.DrugEntity>,
    offlineProcedures: List<com.example.data.local.db.ProcedureEntity>,
    offlineStudy: List<com.example.data.local.db.StudyItemEntity>,
    onDeleteSaved: (String) -> Unit
) {
    var offlineSearch by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = offlineSearch,
                onValueChange = { offlineSearch = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(bi(lang, "Search offline downloaded content...", "অফলাইনে সংরক্ষিত কন্টেন্ট খুঁজুন...")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = bi(
                        lang,
                        "Offline Ready: ${offlineDrugs.size} drugs, ${offlineProcedures.size} procedures, ${offlineStudy.size} notes, ${savedAnswers.size} saved answers",
                        "অফলাইন প্রস্তুত: ${offlineDrugs.size}টি ওষুধ, ${offlineProcedures.size}টি প্রসিডিউর, ${offlineStudy.size}টি নোট"
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (savedAnswers.isNotEmpty()) {
            item { Text(bi(lang, "Saved AI Answers", "সংরক্ষিত AI উত্তর"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(savedAnswers.filter { offlineSearch.isBlank() || it.question.contains(offlineSearch, true) || it.answerSummary.contains(offlineSearch, true) }, key = { it.id }) { ans ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ans.question, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(ans.answerSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(ans.fullFormattedContent.take(260), style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onDeleteSaved(ans.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete saved answer")
                        }
                    }
                }
            }
        }

        val matchedDrugs = offlineDrugs.filter { offlineSearch.isNotBlank() && (it.genericName.contains(offlineSearch, true) || it.brandNames.contains(offlineSearch, true)) }
        if (matchedDrugs.isNotEmpty()) {
            item { Text(bi(lang, "Offline Drugs Matched", "অফলাইন ওষুধ"), style = MaterialTheme.typography.titleSmall) }
            items(matchedDrugs, key = { it.id }) { d ->
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(d.genericName, style = MaterialTheme.typography.titleSmall)
                        Text(d.dosageReference, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerAndCvTab(
    lang: AppLanguage,
    careerItems: List<com.example.data.local.db.StudyItemEntity>
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var fullName by remember { mutableStateOf("Amina Khatun, BSN, RN") }
    var licenseNo by remember { mutableStateOf("BNMC Reg # (Verify Official Record)") }
    var experience by remember { mutableStateOf("2 Years Staff Nurse — Medical-Surgical & Emergency Ward") }
    var skills by remember { mutableStateOf("IV Cannulation, BLS/CPR, Medication Safety (10 Rights), EHR Documentation, Bilingual Patient Education (EN/BN)") }
    var generatedCv by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(careerItems, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(bi(lang, item.titleEn, item.titleBn), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(bi(lang, item.bodyEn, item.bodyBn), style = MaterialTheme.typography.bodySmall)
                    Text("${item.sourceTitle} • Updated ${item.updatedDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(bi(lang, "Nursing CV & Resume Builder", "নার্সিং সিভি (CV) বিল্ডার"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(fullName, { fullName = it }, label = { Text("Full Name & Credentials") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(licenseNo, { licenseNo = it }, label = { Text("Nursing Council / License Reference") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(experience, { experience = it }, label = { Text("Clinical Experience") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(skills, { skills = it }, label = { Text("Core Nursing Competencies") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            generatedCv = """
                                $fullName
                                License: $licenseNo
                                
                                PROFESSIONAL SUMMARY
                                Dedicated nursing professional committed to evidence-based patient care, clinical safety, and compassionate education.
                                
                                CLINICAL EXPERIENCE
                                • $experience
                                
                                CORE NURSING COMPETENCIES
                                • $skills
                            """.trimIndent()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(bi(lang, "Generate Nursing CV Preview", "নার্সিং সিভি তৈরি করুন"))
                    }
                    if (generatedCv.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                            Text(generatedCv, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(generatedCv)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Copy CV")
                            }
                            OutlinedButton(onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, generatedCv)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Nursing CV"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPortalTab(
    lang: AppLanguage,
    viewModel: NursingViewModel,
    docsCount: Int,
    drugsCount: Int,
    mcqsCount: Int,
    auditLogs: List<com.example.data.local.db.AuditLogEntity>
) {
    var pinInput by remember { mutableStateOf("") }
    var unlocked by remember { mutableStateOf(false) }

    var topicTitle by remember { mutableStateOf("") }
    var topicSubject by remember { mutableStateOf(KnowledgeSeedData.subjects.first()) }
    var topicDef by remember { mutableStateOf("") }
    var topicNursing by remember { mutableStateOf("") }
    var topicSource by remember { mutableStateOf("") }

    var drugName by remember { mutableStateOf("") }
    var drugBrands by remember { mutableStateOf("") }
    var drugClass by remember { mutableStateOf("") }
    var drugDose by remember { mutableStateOf("") }
    var drugNursing by remember { mutableStateOf("") }

    var reportTitle by remember { mutableStateOf("") }
    var reportNotes by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!unlocked) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(bi(lang, "Secure Clinical Admin Dashboard", "সুরক্ষিত অ্যাডমিন ড্যাশবোর্ড"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = bi(lang, "Enter Reviewer PIN (demo PIN: 2026) to manage topics, drugs, MCQs, verification & audit logs.", "কন্টেন্ট রিভিউ ও ম্যানেজমেন্টের জন্য অ্যাডমিন পিন দিন (ডেমো পিন: 2026)।"),
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { pinInput = it },
                            label = { Text("Admin PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                        )
                        Button(
                            onClick = { if (pinInput.trim() == "2026") unlocked = true },
                            modifier = Modifier.testTag("unlock_admin_button")
                        ) {
                            Text(bi(lang, "Unlock Admin Portal", "অ্যাডমিন পোর্টাল খুলুন"))
                        }
                    }
                }
            }
        } else {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatBadge("KB Topics", "$docsCount")
                        StatBadge("Drugs", "$drugsCount")
                        StatBadge("MCQs", "$mcqsCount")
                        StatBadge("Audit Logs", "${auditLogs.size}")
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Add Verified Knowledge Source (v1)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        OutlinedTextField(topicTitle, { topicTitle = it }, label = { Text("Topic Title") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(topicSubject, { topicSubject = it }, label = { Text("Nursing Subject") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(topicDef, { topicDef = it }, label = { Text("Clinical Definition") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(topicNursing, { topicNursing = it }, label = { Text("Nursing Management") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(topicSource, { topicSource = it }, label = { Text("Reference / Guideline Source") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = {
                                if (topicTitle.isNotBlank() && topicDef.isNotBlank()) {
                                    viewModel.adminSaveKnowledgeTopic(topicTitle, topicTitle, topicSubject, topicDef, topicNursing, topicSource, "Verified")
                                    topicTitle = ""; topicDef = ""; topicNursing = ""; topicSource = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Publish & Log Version") }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Add Verified Drug Monograph", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        OutlinedTextField(drugName, { drugName = it }, label = { Text("Generic Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(drugBrands, { drugBrands = it }, label = { Text("Brand Names") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(drugClass, { drugClass = it }, label = { Text("Classification") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(drugDose, { drugDose = it }, label = { Text("Dosage Reference") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(drugNursing, { drugNursing = it }, label = { Text("Nursing Responsibilities") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = {
                                if (drugName.isNotBlank()) {
                                    viewModel.adminSaveDrug(drugName, drugBrands, drugClass, drugDose, drugNursing)
                                    drugName = ""; drugBrands = ""; drugClass = ""; drugDose = ""; drugNursing = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save Drug to Formulary") }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Submit Clinical QA Report / Feedback", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        OutlinedTextField(reportTitle, { reportTitle = it }, label = { Text("Topic / Screen") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(reportNotes, { reportNotes = it }, label = { Text("Clinical Review Note") }, modifier = Modifier.fillMaxWidth())
                        OutlinedButton(
                            onClick = {
                                if (reportTitle.isNotBlank()) {
                                    viewModel.submitClinicalFeedback(reportTitle, reportNotes)
                                    reportTitle = ""; reportNotes = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Log Review Feedback") }
                    }
                }
            }

            item { Text("Reviewer Audit Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(auditLogs, key = { it.id }) { log ->
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                        Text("${log.action} • ${log.entityType}: ${log.entityTitle}", style = MaterialTheme.typography.labelLarge)
                        Text("${log.actorRole} — ${log.notes}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

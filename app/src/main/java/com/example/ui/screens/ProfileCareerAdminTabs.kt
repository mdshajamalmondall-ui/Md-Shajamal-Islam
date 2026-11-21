package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.data.local.db.AuditLogEntity
import com.example.data.local.db.KnowledgeSeedData
import com.example.data.local.db.StudyItemEntity
import com.example.ui.viewmodel.NursingViewModel

@Composable
internal fun CareerAndAdminContainerTab(
    lang: AppLanguage,
    viewModel: NursingViewModel,
    careerItems: List<StudyItemEntity>,
    docsCount: Int,
    drugsCount: Int,
    mcqsCount: Int,
    auditLogs: List<AuditLogEntity>
) {
    var mode by remember { mutableIntStateOf(0) } // 0 = Career & CV, 1 = Admin Portal
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = mode == 0,
                onClick = { mode = 0 },
                label = { Text(bi(lang, "Career & CV Builder", "ক্যারিয়ার ও সিভি")) }
            )
            FilterChip(
                selected = mode == 1,
                onClick = { mode = 1 },
                label = { Text(bi(lang, "Admin Portal", "অ্যাডমিন পোর্টাল")) }
            )
        }
        if (mode == 0) {
            CareerAndCvTab(lang = lang, careerItems = careerItems)
        } else {
            AdminPortalTab(
                lang = lang,
                viewModel = viewModel,
                docsCount = docsCount,
                drugsCount = drugsCount,
                mcqsCount = mcqsCount,
                auditLogs = auditLogs
            )
        }
    }
}

@Composable
internal fun CareerAndCvTab(
    lang: AppLanguage,
    careerItems: List<StudyItemEntity>
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
internal fun AdminPortalTab(
    lang: AppLanguage,
    viewModel: NursingViewModel,
    docsCount: Int,
    drugsCount: Int,
    mcqsCount: Int,
    auditLogs: List<AuditLogEntity>
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

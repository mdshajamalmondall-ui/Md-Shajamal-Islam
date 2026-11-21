package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.data.local.db.DrugEntity
import com.example.data.local.db.McqQuestionEntity
import com.example.data.local.db.ProcedureEntity
import com.example.data.local.db.StudyItemEntity
import com.example.ui.components.ClinicalSafetyBanner
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalGreenContainer
import com.example.ui.viewmodel.NursingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyHubScreen(
    viewModel: NursingViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = prefs.language
    val studyItems by viewModel.studyItems.collectAsStateWithLifecycle()
    val drugs by viewModel.drugs.collectAsStateWithLifecycle()
    val procedures by viewModel.procedures.collectAsStateWithLifecycle()
    val mcqs by viewModel.mcqs.collectAsStateWithLifecycle()

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
                    text = bi(lang, "Nursing Study Hub & Clinical Library", "নার্সিং স্টাডি হাব ও ক্লিনিক্যাল লাইব্রেরি"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(bi(lang, "Student Zone", "স্টুডেন্ট জোন")) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(bi(lang, "Notes & Q&A", "নোটস ও প্রশ্নোত্তর")) })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(bi(lang, "Drug Database", "ওষুধের তথ্য")) })
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text(bi(lang, "Procedures", "প্রসিডিউর")) })
                }
            }
        }

        when (selectedTab) {
            0 -> StudentZoneTab(
                lang = lang,
                mcqs = mcqs,
                completedToday = prefs.completedTodayQuestions,
                dailyGoal = prefs.dailyGoalQuestions,
                streak = prefs.studyStreakDays,
                onRecordAnswer = viewModel::recordMcqAnswer
            )
            1 -> NotesAndResourcesTab(
                lang = lang,
                items = studyItems.filter { it.itemType !in listOf("CAREER", "EXAM", "SCHOLARSHIP") },
                onToggleBookmark = viewModel::toggleStudyBookmark,
                onToggleOffline = viewModel::toggleStudyOffline
            )
            2 -> DrugDatabaseTab(
                lang = lang,
                drugs = drugs,
                onToggleOffline = viewModel::toggleDrugOffline
            )
            3 -> ProceduresTab(
                lang = lang,
                procedures = procedures,
                onToggleOffline = viewModel::toggleProcedureOffline
            )
        }
    }
}

@Composable
internal fun StudentZoneTab(
    lang: AppLanguage,
    mcqs: List<McqQuestionEntity>,
    completedToday: Int,
    dailyGoal: Int,
    streak: Int,
    onRecordAnswer: (McqQuestionEntity, Boolean) -> Unit
) {
    var mode by remember { mutableStateOf("MCQ") } // "MCQ" or "FLASHCARD"
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOpt by remember { mutableStateOf<Int?>(null) }
    var cardFlipped by remember { mutableStateOf(false) }

    val weakTopics = remember(mcqs) {
        mcqs.filter { it.attempts > 0 && (it.correctCount.toFloat() / it.attempts) < 0.7f }
            .map { it.subject }
            .distinct()
    }

    LazyColumn(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(bi(lang, "Personal Study Dashboard", "ব্যক্তিগত স্টাডি ড্যাশবোর্ড"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(bi(lang, "Daily Goal: $completedToday / $dailyGoal questions", "দৈনিক লক্ষ্য: $completedToday / $dailyGoal প্রশ্ন"), style = MaterialTheme.typography.bodySmall)
                        }
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
                            Text("🔥 $streak ${bi(lang, "days", "দিন")}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (completedToday.toFloat() / dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                    if (weakTopics.isNotEmpty()) {
                        Text(
                            text = bi(lang, "Weak Topic Detection: Focus on ${weakTopics.joinToString()}", "দুর্বল বিষয় শনাক্তকরণ: ${weakTopics.joinToString()}"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = bi(lang, "Spaced Repetition Active: Master cards across Boxes 1–5.", "স্পেসড রিপিটেশন সক্রিয়: বক্স ১–৫ অনুযায়ী অনুশীলন করুন।"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = mode == "MCQ",
                    onClick = { mode = "MCQ" },
                    label = { Text(bi(lang, "Mock Exam / MCQ", "MCQ ও মডেল টেস্ট")) },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = mode == "FLASHCARD",
                    onClick = { mode = "FLASHCARD"; cardFlipped = false },
                    label = { Text(bi(lang, "Spaced Flashcards", "ফ্ল্যাশকার্ড")) },
                    leadingIcon = { Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        if (mcqs.isNotEmpty()) {
            val q = mcqs[currentIndex % mcqs.size]
            if (mode == "MCQ") {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${q.subject} • ${q.difficulty}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text("${(currentIndex % mcqs.size) + 1} / ${mcqs.size}", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(bi(lang, q.questionEn, q.questionBn), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            listOf(q.optionA, q.optionB, q.optionC, q.optionD).forEachIndexed { idx, opt ->
                                val isChosen = selectedOpt == idx
                                val isRight = idx == q.correctIndex
                                val bg = when {
                                    selectedOpt == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    isRight -> ClinicalGreenContainer
                                    isChosen -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = bg,
                                    modifier = Modifier.fillMaxWidth().clickable(enabled = selectedOpt == null) {
                                        selectedOpt = idx
                                        onRecordAnswer(q, idx == q.correctIndex)
                                    }
                                ) {
                                    Text("${('A' + idx)}. $opt", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                                }
                            }
                            if (selectedOpt != null) {
                                Text(bi(lang, q.rationaleEn, q.rationaleBn), style = MaterialTheme.typography.bodySmall, color = ClinicalGreen)
                                Text("Source: ${q.sourceTitle} (${q.verificationStatus})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    selectedOpt = null
                                    currentIndex = (currentIndex + 1) % mcqs.size
                                },
                                modifier = Modifier.fillMaxWidth().testTag("next_mcq_button")
                            ) {
                                Text(bi(lang, "Next Question", "পরবর্তী প্রশ্ন"))
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth().clickable { cardFlipped = !cardFlipped }
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Box ${q.masteryBox}/5 • ${q.subject}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (!cardFlipped) bi(lang, q.questionEn, q.questionBn) else bi(lang, q.rationaleEn, q.rationaleBn),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = bi(lang, if (cardFlipped) "Tap to see question" else "Tap card to reveal rationale", if (cardFlipped) "প্রশ্ন দেখতে ট্যাপ করুন" else "উত্তর ও ব্যাখ্যা দেখতে ট্যাপ করুন"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        onRecordAnswer(q, false)
                                        cardFlipped = false
                                        currentIndex = (currentIndex + 1) % mcqs.size
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text(bi(lang, "Review Again", "আবার পড়ব")) }
                                Button(
                                    onClick = {
                                        onRecordAnswer(q, true)
                                        cardFlipped = false
                                        currentIndex = (currentIndex + 1) % mcqs.size
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text(bi(lang, "Mastered ✓", "আয়ত্ত হয়েছে ✓")) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NotesAndResourcesTab(
    lang: AppLanguage,
    items: List<StudyItemEntity>,
    onToggleBookmark: (StudyItemEntity) -> Unit,
    onToggleOffline: (StudyItemEntity) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ALL") }

    val types = listOf("ALL", "NOTE", "BOOK", "SHORT_QA", "BROAD_QA", "VIVA", "CASE_STUDY", "ASSIGNMENT", "CARE_PLAN", "NANDA", "DEFINITION")
    val filtered = remember(items, search, selectedType) {
        items.filter { item ->
            (selectedType == "ALL" || item.itemType == selectedType) &&
                (search.isBlank() || item.titleEn.contains(search, true) || item.titleBn.contains(search, true) || item.bodyEn.contains(search, true) || item.subject.contains(search, true))
        }
    }

    LazyColumn(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(bi(lang, "Search notes, viva, care plans, NANDA...", "নোটস, ভাইভা, কেয়ার প্ল্যান বা NANDA খুঁজুন...")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(types) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.replace('_', ' ')) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("${item.itemType} • ${item.subject}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Row {
                            IconButton(onClick = { onToggleOffline(item) }) {
                                Icon(
                                    imageVector = if (item.isOfflineSaved) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                    contentDescription = "Offline status",
                                    tint = if (item.isOfflineSaved) ClinicalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onToggleBookmark(item) }) {
                                Icon(
                                    imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Text(bi(lang, item.titleEn, item.titleBn), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(bi(lang, item.bodyEn, item.bodyBn), style = MaterialTheme.typography.bodyMedium)
                    Text("Source: ${item.sourceTitle} • ${item.verificationStatus} (${item.updatedDate})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun DrugDatabaseTab(
    lang: AppLanguage,
    drugs: List<DrugEntity>,
    onToggleOffline: (DrugEntity) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<String?>(null) }

    val filtered = remember(drugs, search) {
        drugs.filter {
            search.isBlank() || it.genericName.contains(search, true) || it.brandNames.contains(search, true) || it.classification.contains(search, true)
        }
    }

    LazyColumn(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ClinicalSafetyBanner(language = lang)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(bi(lang, "Search generic, brand, or drug class...", "জেনেরিক, ব্র্যান্ড বা ওষুধের শ্রেণি খুঁজুন...")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("drug_search_input")
            )
        }

        items(filtered, key = { it.id }) { drug ->
            val isExpanded = expandedId == drug.id
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (drug.isHighAlert) MaterialTheme.colorScheme.error.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().clickable { expandedId = if (isExpanded) null else drug.id }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Medication, contentDescription = null, tint = if (drug.isHighAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(drug.genericName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${drug.brandNames} • ${drug.classification}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { onToggleOffline(drug) }) {
                            Icon(
                                imageVector = if (drug.isOfflineSaved) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                contentDescription = "Offline drug save",
                                tint = if (drug.isOfflineSaved) ClinicalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (drug.isHighAlert) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("HIGH-ALERT MEDICATION • Double-check required", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }

                    if (isExpanded) {
                        DrugDetailLine("Indications", drug.indications)
                        DrugDetailLine("Mechanism of Action", drug.mechanismOfAction)
                        DrugDetailLine("Dosage Reference", drug.dosageReference)
                        DrugDetailLine("Route", drug.route)
                        DrugDetailLine("Contraindications", drug.contraindications)
                        DrugDetailLine("Side Effects", drug.sideEffects)
                        DrugDetailLine("Adverse Reactions", drug.adverseReactions)
                        DrugDetailLine("Nursing Responsibilities", drug.nursingResponsibilities)
                        DrugDetailLine("Drug Interactions", drug.drugInteractions)
                        DrugDetailLine("Patient Counseling", drug.patientCounseling)
                        DrugDetailLine("Storage", drug.storageInfo)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ClinicalGreen, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${drug.sourceTitle} • ${drug.verificationStatus} (${drug.updatedDate})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Text(bi(lang, "Tap to view full pharmacology monograph", "সম্পূর্ণ মনোওগ্রাফ দেখতে ট্যাপ করুন"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DrugDetailLine(label: String, value: String) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun ProceduresTab(
    lang: AppLanguage,
    procedures: List<ProcedureEntity>,
    onToggleOffline: (ProcedureEntity) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<String?>(null) }

    val filtered = remember(procedures, search) {
        procedures.filter {
            search.isBlank() || it.titleEn.contains(search, true) || it.titleBn.contains(search, true) || it.category.contains(search, true)
        }
    }

    LazyColumn(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(bi(lang, "Search nursing procedures...", "নার্সিং প্রসিডিউর খুঁজুন...")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(filtered, key = { it.id }) { proc ->
            val isExpanded = expandedId == proc.id
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().clickable { expandedId = if (isExpanded) null else proc.id }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bi(lang, proc.titleEn, proc.titleBn), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(proc.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onToggleOffline(proc) }) {
                            Icon(
                                imageVector = if (proc.isOfflineSaved) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                contentDescription = "Offline save",
                                tint = if (proc.isOfflineSaved) ClinicalGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (proc.supervisedTrainingRequired) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                text = bi(lang, "Supervised Clinical Training Required", "তত্ত্বাবধানে ক্লিনিক্যাল প্রশিক্ষণ প্রয়োজন"),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (isExpanded) {
                        if (proc.drawableKey == "img_procedure_iv") {
                            Image(
                                painter = painterResource(id = R.drawable.img_procedure_iv),
                                contentDescription = proc.titleEn,
                                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        DrugDetailLine("Definition", proc.definition)
                        DrugDetailLine("Purpose", proc.purpose)
                        DrugDetailLine("Equipment", proc.equipment)
                        DrugDetailLine("Preparation", proc.preparation)
                        DrugDetailLine("Step-by-Step Procedure", proc.steps)
                        DrugDetailLine("Nursing Responsibilities", proc.nursingResponsibilities)
                        DrugDetailLine("Infection Control", proc.infectionControl)
                        DrugDetailLine("Complications", proc.complications)
                        DrugDetailLine("Documentation", proc.documentation)
                        Text("${proc.sourceTitle} • ${proc.verificationStatus} (${proc.updatedDate})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(proc.definition, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }
    }
}

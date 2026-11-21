package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.AppLanguage
import com.example.data.local.ThemeMode
import com.example.data.local.UserPreferences
import com.example.data.local.UserRole
import com.example.data.local.bi
import com.example.data.local.db.PersonalNoteEntity
import com.example.data.local.db.SavedAnswerEntity
import com.example.ui.components.ClinicalSafetyBanner
import com.example.ui.components.FirebaseAiNoticeBanner
import com.example.ui.theme.ClinicalGreen
import com.example.ui.viewmodel.NursingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: NursingViewModel,
    onAskQuestionInSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = prefs.language
    val savedAnswers by viewModel.savedAnswers.collectAsStateWithLifecycle()
    val personalNotes by viewModel.personalNotes.collectAsStateWithLifecycle()
    val studyItems by viewModel.studyItems.collectAsStateWithLifecycle()
    val drugs by viewModel.drugs.collectAsStateWithLifecycle()
    val knowledgeDocs by viewModel.knowledgeDocs.collectAsStateWithLifecycle()
    val mcqs by viewModel.mcqs.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var editingNote by remember { mutableStateOf<PersonalNoteEntity?>(null) }
    var showNewNoteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.updateProfilePhoto(uri)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showNewNoteDialog = true },
                    modifier = Modifier.testTag("add_personal_note_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = bi(lang, "Add Note", "নোট যোগ করুন"))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .padding(top = 12.dp)
                ) {
                    StudentProfileBanner(
                        prefs = prefs,
                        lang = lang,
                        onPickPhoto = {
                            avatarPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(bi(lang, "Profile Info", "প্রোফাইল তথ্য")) },
                            modifier = Modifier.testTag("profile_tab_info")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(bi(lang, "My Notes (${personalNotes.size})", "আমার নোট (${personalNotes.size})")) },
                            modifier = Modifier.testTag("profile_tab_notes")
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text(bi(lang, "Saved Q&A (${savedAnswers.size})", "সেভ করা প্রশ্ন (${savedAnswers.size})")) },
                            modifier = Modifier.testTag("profile_tab_saved")
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text(bi(lang, "Career & Admin", "ক্যারিয়ার ও অ্যাডমিন")) },
                            modifier = Modifier.testTag("profile_tab_admin")
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> ProfileDetailsTab(prefs = prefs, viewModel = viewModel)
                1 -> PersonalNotesTab(
                    lang = lang,
                    notes = personalNotes,
                    onAddClick = { showNewNoteDialog = true },
                    onEditClick = { editingNote = it },
                    onDeleteClick = viewModel::deleteNote
                )
                2 -> SavedQuestionsTab(
                    lang = lang,
                    savedAnswers = savedAnswers,
                    onReaskInSearch = onAskQuestionInSearch,
                    onDeleteSaved = viewModel::deleteSavedAnswer
                )
                3 -> CareerAndAdminContainerTab(
                    lang = lang,
                    viewModel = viewModel,
                    careerItems = studyItems.filter { it.itemType in listOf("CAREER", "EXAM", "SCHOLARSHIP") },
                    docsCount = knowledgeDocs.size,
                    drugsCount = drugs.size,
                    mcqsCount = mcqs.size,
                    auditLogs = auditLogs
                )
            }
        }
    }

    if (showNewNoteDialog || editingNote != null) {
        PersonalNoteEditorDialog(
            lang = lang,
            existing = editingNote,
            onDismiss = {
                showNewNoteDialog = false
                editingNote = null
            },
            onSave = { title, content, tag ->
                viewModel.addOrUpdateNote(
                    id = editingNote?.id,
                    title = title,
                    content = content,
                    tag = tag
                )
                showNewNoteDialog = false
                editingNote = null
            }
        )
    }
}

@Composable
private fun StudentProfileBanner(
    prefs: UserPreferences,
    lang: AppLanguage,
    onPickPhoto: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clickable(onClick = onPickPhoto)
                .testTag("profile_avatar_picker")
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxSize()
            ) {
                if (prefs.profilePhotoUri.isNotBlank()) {
                    AsyncImage(
                        model = prefs.profilePhotoUri,
                        contentDescription = bi(lang, "Student profile picture", "ছাত্র/ছাত্রীর প্রোফাইল ছবি"),
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = bi(lang, "Change photo", "ছবি পরিবর্তন করুন"),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = prefs.userName.ifBlank { "Nursing Student" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = bi(lang, prefs.userRole.labelEn, prefs.userRole.labelBn) +
                    if (prefs.studentBatch.isNotBlank()) " • ${prefs.studentBatch}" else "",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (prefs.email.isNotBlank() || prefs.phoneNumber.isNotBlank()) {
                Text(
                    text = listOf(prefs.email, prefs.phoneNumber).filter { it.isNotBlank() }.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = bi(lang, "Tap photo to set Student Picture", "প্রোফাইল ছবি যোগ করতে ছবির ওপর ট্যাপ করুন"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileDetailsTab(
    prefs: UserPreferences,
    viewModel: NursingViewModel
) {
    val lang = prefs.language
    var nameInput by remember(prefs.userName) { mutableStateOf(prefs.userName) }
    var emailInput by remember(prefs.email) { mutableStateOf(prefs.email) }
    var phoneInput by remember(prefs.phoneNumber) { mutableStateOf(prefs.phoneNumber) }
    var institutionInput by remember(prefs.institution) { mutableStateOf(prefs.institution) }
    var batchInput by remember(prefs.studentBatch) { mutableStateOf(prefs.studentBatch) }
    var selectedRole by remember(prefs.userRole) { mutableStateOf(prefs.userRole) }
    var savedToast by remember { mutableStateOf(false) }

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
                    Text(
                        text = bi(lang, "Student & Nurse Profile Information", "স্টুডেন্ট ও নার্স প্রোফাইল তথ্য"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it; savedToast = false },
                        label = { Text(bi(lang, "Full Name", "পুরো নাম")) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it; savedToast = false },
                        label = { Text(bi(lang, "Gmail / Email Address", "জিমেইল / ইমেইল")) },
                        placeholder = { Text("student@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_email_input")
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it; savedToast = false },
                        label = { Text(bi(lang, "Phone Number", "ফোন নম্বর")) },
                        placeholder = { Text("+880 1XXX-XXXXXX") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_phone_input")
                    )
                    OutlinedTextField(
                        value = institutionInput,
                        onValueChange = { institutionInput = it; savedToast = false },
                        label = { Text(bi(lang, "Nursing College / Hospital", "নার্সিং কলেজ / হাসপাতাল")) },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_institution_input")
                    )
                    OutlinedTextField(
                        value = batchInput,
                        onValueChange = { batchInput = it; savedToast = false },
                        label = { Text(bi(lang, "Student Batch / Program / Year", "ব্যাচ / কোর্স / বর্ষ")) },
                        placeholder = { Text("B.Sc. in Nursing — 3rd Year") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_batch_input")
                    )

                    Text(bi(lang, "Role", "পেশাগত পরিচয়"), style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        UserRole.entries.forEach { role ->
                            FilterChip(
                                selected = selectedRole == role,
                                onClick = { selectedRole = role; savedToast = false },
                                label = { Text(bi(lang, role.labelEn, role.labelBn)) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateUserProfile(
                                    name = nameInput,
                                    email = emailInput,
                                    phone = phoneInput,
                                    institution = institutionInput,
                                    studentBatch = batchInput,
                                    role = selectedRole
                                )
                                savedToast = true
                            },
                            modifier = Modifier.weight(1f).testTag("save_profile_button")
                        ) {
                            Text(bi(lang, "Save Profile", "প্রোফাইল সংরক্ষণ করুন"))
                        }
                        if (prefs.profilePhotoUri.isNotBlank()) {
                            OutlinedButton(onClick = { viewModel.updateProfilePhoto(null) }) {
                                Text(bi(lang, "Clear Photo", "ছবি সরান"))
                            }
                        }
                    }

                    if (savedToast) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ClinicalGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = bi(lang, "Profile saved!", "প্রোফাইল তথ্য সংরক্ষিত হয়েছে!"),
                                style = MaterialTheme.typography.bodySmall,
                                color = ClinicalGreen
                            )
                        }
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
                    Text(bi(lang, "Language & Theme", "ভাষা ও থিম"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
private fun PersonalNotesTab(
    lang: AppLanguage,
    notes: List<PersonalNoteEntity>,
    onAddClick: () -> Unit,
    onEditClick: (PersonalNoteEntity) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var search by remember { mutableStateOf("") }
    val filtered = remember(notes, search) {
        notes.filter {
            search.isBlank() || it.title.contains(search, true) || it.content.contains(search, true) || it.tag.contains(search, true)
        }
    }

    LazyColumn(
        modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text(bi(lang, "Search your notes...", "আপনার নোট খুঁজুন...")) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onAddClick, modifier = Modifier.testTag("new_note_button")) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(bi(lang, "New", "নতুন"))
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.NoteAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Text(bi(lang, "No personal nursing notes yet", "এখনো কোনো ব্যক্তিগত নোট নেই"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            bi(lang, "Jot down lecture highlights, clinical pearls, drug doses, or viva questions here.", "ক্লাসের নোট, ক্লিনিক্যাল তথ্য বা ভাইভা প্রশ্ন লিখে সংরক্ষণ করুন।"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { note ->
                val dateStr = remember(note.updatedAt) {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(note.updatedAt))
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                                Text(note.tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            IconButton(onClick = { clipboard.setText(AnnotatedString("${note.title}\n\n${note.content}")) }, modifier = Modifier.minimumInteractiveComponentSize()) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy note")
                            }
                            IconButton(onClick = { onEditClick(note) }, modifier = Modifier.minimumInteractiveComponentSize()) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit note")
                            }
                            IconButton(onClick = { onDeleteClick(note.id) }, modifier = Modifier.minimumInteractiveComponentSize()) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete note", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Text(note.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(note.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonalNoteEditorDialog(
    lang: AppLanguage,
    existing: PersonalNoteEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, tag: String) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var content by remember { mutableStateOf(existing?.content.orEmpty()) }
    var tag by remember { mutableStateOf(existing?.tag ?: "Lecture Note") }
    val tags = listOf("Lecture Note", "Clinical Pearl", "Exam Prep", "Drug Note", "Viva Q&A")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existing == null) bi(lang, "Add Nursing Note", "নতুন নোট যোগ করুন")
                else bi(lang, "Edit Note", "নোট সম্পাদনা করুন")
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(bi(lang, "Note Title", "নোটের শিরোনাম")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { t ->
                        FilterChip(selected = tag == t, onClick = { tag = t }, label = { Text(t) })
                    }
                }
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(bi(lang, "Write your note...", "বিস্তারিত লিখুন...")) },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth().testTag("note_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, tag) },
                enabled = title.isNotBlank() || content.isNotBlank(),
                modifier = Modifier.testTag("save_note_dialog_button")
            ) {
                Text(bi(lang, "Save", "সংরক্ষণ"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(bi(lang, "Cancel", "বাতিল"))
            }
        }
    )
}

@Composable
private fun SavedQuestionsTab(
    lang: AppLanguage,
    savedAnswers: List<SavedAnswerEntity>,
    onReaskInSearch: (String) -> Unit,
    onDeleteSaved: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var search by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<String?>(null) }

    val filtered = remember(savedAnswers, search) {
        savedAnswers.filter {
            search.isBlank() || it.question.contains(search, true) || it.answerSummary.contains(search, true)
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
                placeholder = { Text(bi(lang, "Search saved questions...", "সংরক্ষিত প্রশ্ন খুঁজুন...")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (filtered.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Text(bi(lang, "No saved questions yet", "এখনো কোনো প্রশ্ন সেভ করা হয়নি"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            bi(lang, "Tap the bookmark icon on any answer in Search to save your favorite questions here.", "সার্চ করার পর যেকোনো উত্তরের বুকমার্ক আইকনে চাপ দিয়ে এখানে সংরক্ষণ করুন।"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { ans ->
                val expanded = expandedId == ans.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().clickable { expandedId = if (expanded) null else ans.id }
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(ans.question, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(ans.answerSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (expanded) ans.fullFormattedContent else ans.fullFormattedContent.take(220) + if (ans.fullFormattedContent.length > 220) "..." else "",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onReaskInSearch(ans.question) }) {
                                Text(bi(lang, "Open in Search", "সার্চে খুলুন"))
                            }
                            Row {
                                IconButton(onClick = { clipboard.setText(AnnotatedString("${ans.question}\n\n${ans.fullFormattedContent}")) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                                IconButton(onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "${ans.question}\n\n${ans.fullFormattedContent}")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Saved Question"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                                IconButton(onClick = { onDeleteSaved(ans.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

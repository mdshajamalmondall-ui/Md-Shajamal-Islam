package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.data.local.db.ChatMessageEntity
import com.example.ui.components.ClinicalSafetyBanner
import com.example.ui.components.FirebaseAiNoticeBanner
import com.example.ui.components.NursingSearchComposer
import com.example.ui.components.StructuredAiMessageCard
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalGreenContainer
import com.example.ui.viewmodel.NursingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: NursingViewModel,
    onNavigateToClinicalTools: () -> Unit,
    onNavigateToStudy: () -> Unit,
    onNavigateToAiHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = prefs.language
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val threads by viewModel.threads.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val savedAnswers by viewModel.savedAnswers.collectAsStateWithLifecycle()
    val mcqs by viewModel.mcqs.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isChatGenerating.collectAsStateWithLifecycle()
    val attachedImageUri by viewModel.attachedImageUri.collectAsStateWithLifecycle()

    var queryText by remember { mutableStateOf("") }
    var showHistorySheet by remember { mutableStateOf(false) }
    var voiceStatusMessage by remember { mutableStateOf<String?>(null) }
    var isListeningVoice by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.attachImage(uri)
        }
    }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(speechRecognizer) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    fun startVoiceRecognition() {
        if (speechRecognizer == null) {
            voiceStatusMessage = bi(
                lang,
                "Speech recognition service unavailable on this device. Please type your nursing question.",
                "এই ডিভাইসে ভয়েস রিকগনিশন সেবা পাওয়া যায়নি। অনুগ্রহ করে টাইপ করে প্রশ্ন করুন।"
            )
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == AppLanguage.BENGALI) "bn-BD" else "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListeningVoice = true
                voiceStatusMessage = bi(lang, "Listening... Speak your nursing question.", "শুনছি... আপনার নার্সিং প্রশ্নটি বলুন।")
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListeningVoice = false }
            override fun onError(error: Int) {
                isListeningVoice = false
                voiceStatusMessage = bi(lang, "Voice capture stopped. Tap mic to retry.", "ভয়েস শোনা যায়নি। আবার চেষ্টা করুন।")
            }
            override fun onResults(results: Bundle?) {
                isListeningVoice = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = matches?.firstOrNull().orEmpty()
                if (spoken.isNotBlank()) {
                    queryText = spoken
                    voiceStatusMessage = null
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!partial.isNullOrBlank()) queryText = partial
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer.startListening(intent)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceRecognition()
        } else {
            voiceStatusMessage = bi(
                lang,
                "Microphone permission denied. Enable microphone permission in settings to use Voice Search.",
                "মাইক্রোফোন অনুমতি দেওয়া হয়নি। ভয়েস সার্চের জন্য সেটিংস থেকে অনুমতি দিন।"
            )
        }
    }

    fun onVoiceClicked() {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            startVoiceRecognition()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            listState.animateScrollToItem(activeMessages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "Nursing AI",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NURSING AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = bi(lang, "Clinical & Nursing Knowledge Engine", "স্মার্ট নার্সিং শিক্ষা ও ক্লিনিক্যাল সহায়ক"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {
                            val next = if (lang == AppLanguage.ENGLISH) AppLanguage.BENGALI else AppLanguage.ENGLISH
                            viewModel.setLanguage(next)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = {
                            Text(if (lang == AppLanguage.ENGLISH) "EN" else "বাংলা")
                        },
                        modifier = Modifier.testTag("home_language_toggle")
                    )
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = bi(lang, "Chat History", "আলোচনার ইতিহাস")
                        )
                    }
                    if (activeMessages.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.startNewChat() },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("new_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddComment,
                                contentDescription = bi(lang, "New Chat", "নতুন আলোচনা"),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        if (voiceStatusMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = voiceStatusMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { voiceStatusMessage = null }) {
                        Text(bi(lang, "Dismiss", "বন্ধ করুন"))
                    }
                }
            }
        }

        if (activeMessages.isEmpty()) {
            // ChatGPT-Style Universal Nursing Search Homepage
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 760.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Hero Banner + Search Focus
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(145.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_hero_nursing),
                                    contentDescription = "Nursing AI Educator",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                                                )
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = bi(
                                            lang,
                                            "Ask anything about Nursing",
                                            "নার্সিং সম্পর্কিত যেকোনো প্রশ্ন করুন"
                                        ),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = bi(
                                            lang,
                                            "Search diseases, pharmacology, procedures, care plans, or upload a study photo.",
                                            "রোগ, ওষুধ, প্রসিডিউর, কেয়ার প্ল্যান বা বইয়ের ছবি আপলোড করে উত্তর পান।"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(modifier = Modifier.padding(12.dp)) {
                                NursingSearchComposer(
                                    query = queryText,
                                    onQueryChange = { queryText = it },
                                    onSend = {
                                        val q = queryText
                                        queryText = ""
                                        viewModel.sendChatQuery(q)
                                    },
                                    onVoiceClick = { onVoiceClicked() },
                                    onImagePickClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    attachedImageUri = attachedImageUri,
                                    onClearImage = { viewModel.attachImage(null) },
                                    language = lang,
                                    isLoading = isGenerating
                                )
                            }
                        }
                    }
                }

                item {
                    FirebaseAiNoticeBanner(language = lang)
                }

                // Suggested Nursing Questions
                item {
                    SectionHeading(
                        title = bi(lang, "Suggested Nursing Questions", "জনপ্রিয় নার্সিং প্রশ্নসমূহ")
                    )
                    val suggestions = if (lang == AppLanguage.BENGALI) {
                        listOf(
                            "উচ্চ রক্তচাপের নার্সিং ব্যবস্থাপনা কী?",
                            "ডায়াবেটিক কিটোঅ্যাসিডোসিস (DKA) ও ইনসুলিন সতর্কতা",
                            "প্রসব-পরবর্তী রক্তক্ষরণ (PPH) ও এক্লাম্পসিয়া প্রোটোকল",
                            "পেরিফেরাল IV ক্যানুলেশন ধাপে ধাপে নিয়ম",
                            "Ceftriaxone ওষুধের ডোজ ও নার্সিং দায়িত্ব",
                            "WHO হ্যান্ড হাইজিনের ৫টি মুহূর্ত কী কী?"
                        )
                    } else {
                        listOf(
                            "Nursing management of Hypertension & Crisis",
                            "DKA insulin infusion & potassium safety rule",
                            "Postpartum Hemorrhage (PPH) & MgSO4 toxicity",
                            "Peripheral IV Cannulation step-by-step guide",
                            "Ceftriaxone monograph & nursing responsibilities",
                            "Pneumonia & WHO 5 Moments of Hand Hygiene"
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { prompt ->
                            AssistChip(
                                onClick = { viewModel.sendChatQuery(prompt) },
                                label = {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                            )
                        }
                    }
                }

                // Recent Searches
                if (searchHistory.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeading(title = bi(lang, "Recent Searches", "সাম্প্রতিক সার্চ"))
                            TextButton(onClick = { viewModel.clearSearchHistory() }) {
                                Text(bi(lang, "Clear", "মুছুন"))
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(searchHistory.take(8)) { entry ->
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.sendChatQuery(entry.query) },
                                    label = { Text(entry.query, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }

                // Quick Clinical Tools
                item {
                    SectionHeading(title = bi(lang, "Quick Clinical Tools", "দ্রুত ক্লিনিক্যাল টুলস"))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = bi(lang, "IV & Dose Calc", "IV ও ডোজ ক্যালকুলেটর"),
                            subtitle = bi(lang, "gtt/min, mL/hr, Ped", "সহজ ডোজ হিসাব"),
                            icon = Icons.Default.Calculate,
                            onClick = onNavigateToClinicalTools,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = bi(lang, "Care Plan AI", "কেয়ার প্ল্যান AI"),
                            subtitle = bi(lang, "NANDA-I / NIC / NOC", "রোগীর কেয়ার প্ল্যান"),
                            icon = Icons.Default.MedicalServices,
                            onClick = onNavigateToAiHub,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Daily Nursing Quiz Card
                if (mcqs.isNotEmpty()) {
                    item {
                        DailyQuizHomeCard(
                            mcq = mcqs.first(),
                            language = lang,
                            completedToday = prefs.completedTodayQuestions,
                            dailyGoal = prefs.dailyGoalQuestions,
                            streakDays = prefs.studyStreakDays,
                            onAnswer = { correct -> viewModel.recordMcqAnswer(mcqs.first(), correct) },
                            onOpenStudyHub = onNavigateToStudy
                        )
                    }
                }

                // Popular Nursing Topics & Continue Learning
                item {
                    SectionHeading(title = bi(lang, "Popular Nursing Topics", "জনপ্রিয় নার্সিং বিষয়সমূহ"))
                    val topics = listOf(
                        "Medical-Surgical Nursing" to bi(lang, "Hypertension, DKA, COPD", "উচ্চ রক্তচাপ, ডায়াবেটিস, COPD"),
                        "Pharmacology" to bi(lang, "High-Alert Drugs & Antidotes", "হাই-অ্যালার্ট ওষুধ ও অ্যান্টিডোট"),
                        "Maternal & Child" to bi(lang, "PPH, Eclampsia, IMCI", "প্রসব রক্তক্ষরণ, এক্লাম্পসিয়া"),
                        "Infection Control" to bi(lang, "WHO Hand Hygiene & Asepsis", "হ্যান্ড হাইজিন ও অ্যাসেপসিস")
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        topics.forEach { (title, sub) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToStudy() },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, style = MaterialTheme.typography.titleSmall)
                                        Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                // Saved Resources Preview
                if (savedAnswers.isNotEmpty()) {
                    item {
                        SectionHeading(title = bi(lang, "Saved Answers", "সংরক্ষিত উত্তরসমূহ"))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            savedAnswers.take(3).forEach { saved ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.sendChatQuery(saved.question) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(saved.question, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(saved.answerSummary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    ClinicalSafetyBanner(language = lang)
                    Spacer(Modifier.height(12.dp))
                }
            }
        } else {
            // Active Multi-Turn ChatGPT-Style Conversation
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 760.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeMessages, key = { it.id }) { msg ->
                    if (msg.role == "user") {
                        UserChatBubble(message = msg)
                    } else {
                        val lastQ = activeMessages.lastOrNull { it.role == "user" && it.createdAt <= msg.createdAt }?.content.orEmpty()
                        StructuredAiMessageCard(
                            message = msg,
                            userQuestion = lastQ,
                            language = lang,
                            onSave = { viewModel.saveChatMessage(lastQ, msg) },
                            onFollowUpClick = { followUp -> viewModel.sendChatQuery(followUp) }
                        )
                    }
                }

                if (isGenerating) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = bi(lang, "Retrieving nursing references & formatting answer...", "নার্সিং রেফারেন্স খুঁজে উত্তর তৈরি হচ্ছে..."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Sticky Follow-Up Composer
            Box(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                NursingSearchComposer(
                    query = queryText,
                    onQueryChange = { queryText = it },
                    onSend = {
                        val q = queryText
                        queryText = ""
                        viewModel.sendChatQuery(q)
                    },
                    onVoiceClick = { onVoiceClicked() },
                    onImagePickClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    attachedImageUri = attachedImageUri,
                    onClearImage = { viewModel.attachImage(null) },
                    language = lang,
                    isLoading = isGenerating
                )
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bi(lang, "Chat History", "আগের সব আলোচনা"),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(
                        onClick = {
                            viewModel.startNewChat()
                            showHistorySheet = false
                        }
                    ) {
                        Text(bi(lang, "New Chat", "নতুন চ্যাট"))
                    }
                }

                if (threads.isEmpty()) {
                    Text(
                        text = bi(lang, "No previous conversations yet. Ask your first nursing question!", "এখনো কোনো আলোচনা নেই। প্রথম প্রশ্নটি করুন!"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    threads.forEach { thread ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectThread(thread.id)
                                    showHistorySheet = false
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(thread.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        text = if (thread.languageCode == "bn") "বাংলা" else "English",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteThread(thread.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete conversation")
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun UserChatBubble(message: ChatMessageEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!message.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = message.imageUri,
                        contentDescription = "Attached study image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DailyQuizHomeCard(
    mcq: com.example.data.local.db.McqQuestionEntity,
    language: AppLanguage,
    completedToday: Int,
    dailyGoal: Int,
    streakDays: Int,
    onAnswer: (Boolean) -> Unit,
    onOpenStudyHub: () -> Unit
) {
    var selectedIdx by remember { mutableStateOf<Int?>(null) }
    val options = listOf(mcq.optionA, mcq.optionB, mcq.optionC, mcq.optionD)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = bi(language, "Daily Nursing Quiz", "দৈনিক নার্সিং কুইজ"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "🔥 $streakDays d • $completedToday/$dailyGoal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { (completedToday.toFloat() / dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Text(
                text = bi(language, mcq.questionEn, mcq.questionBn),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            options.forEachIndexed { idx, opt ->
                val isSelected = selectedIdx == idx
                val isCorrect = idx == mcq.correctIndex
                val bg = when {
                    selectedIdx == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    isCorrect -> ClinicalGreenContainer
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = bg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedIdx == null) {
                            selectedIdx = idx
                            onAnswer(idx == mcq.correctIndex)
                        }
                ) {
                    Text(
                        text = "${('A' + idx)}. $opt",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            if (selectedIdx != null) {
                Text(
                    text = bi(language, mcq.rationaleEn, mcq.rationaleBn),
                    style = MaterialTheme.typography.bodySmall,
                    color = ClinicalGreen
                )
                OutlinedButton(onClick = onOpenStudyHub, modifier = Modifier.align(Alignment.End)) {
                    Text(bi(language, "Practice More in Study Hub", "স্টাডি হাবে আরও অনুশীলন করুন"))
                }
            }
        }
    }
}

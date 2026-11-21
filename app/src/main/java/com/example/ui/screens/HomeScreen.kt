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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.viewmodel.NursingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: NursingViewModel,
    onNavigateToTools: () -> Unit,
    onNavigateToFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = prefs.language
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val threads by viewModel.threads.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isChatGenerating.collectAsStateWithLifecycle()
    val streamingStatus by viewModel.streamingStatus.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()
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
        if (uri != null) viewModel.attachImage(uri)
    }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(speechRecognizer) {
        onDispose { speechRecognizer?.destroy() }
    }

    fun startVoiceRecognition() {
        if (speechRecognizer == null) {
            voiceStatusMessage = bi(
                lang,
                "Speech recognition unavailable on this device. Please type your nursing question.",
                "এই ডিভাইসে ভয়েস রিকগনিশন পাওয়া যায়নি। টাইপ করে প্রশ্ন করুন।"
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
                voiceStatusMessage = bi(lang, "Listening... Speak your nursing question.", "শুনছি... আপনার প্রশ্নটি বলুন।")
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
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
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
        if (granted) startVoiceRecognition()
        else voiceStatusMessage = bi(lang, "Microphone permission denied.", "মাইক্রোফোন অনুমতি দেওয়া হয়নি।")
    }

    fun onVoiceClicked() {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (hasPerm) startVoiceRecognition() else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
        // Minimalist ChatGPT-style Top Bar
        Row(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = { showHistorySheet = true },
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("chat_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = bi(lang, "Search history", "সার্চ ইতিহাস"),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        val next = if (lang == AppLanguage.ENGLISH) AppLanguage.BENGALI else AppLanguage.ENGLISH
                        viewModel.setLanguage(next)
                    }
                    .testTag("language_toggle_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Nursing AI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = if (lang == AppLanguage.ENGLISH) "EN" else "বাংলা",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = { viewModel.startNewChat(); queryText = "" },
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = bi(lang, "New chat", "নতুন সার্চ"),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Main Chat / Search Canvas
        if (activeMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.app_icon_nursing),
                                contentDescription = "Nursing AI Emblem",
                                modifier = Modifier.size(44.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Text(
                        text = bi(lang, "Ask anything about Nursing", "নার্সিং সম্পর্কে যেকোনো প্রশ্ন করুন"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = bi(
                            lang,
                            "Search diseases, definitions, drugs, procedures, or NANDA-I care in English or বাংলা.",
                            "যেকোনো রোগের সংজ্ঞা, ব্যথা, ওষুধ, প্রসিডিউর বা নার্সিং কেয়ার লিখে সার্চ করুন।"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    val quickPrompts = listOf(
                        bi(lang, "Definition of Nursing", "নার্সিং কাকে বলে?"),
                        bi(lang, "Definition of Pain", "ব্যথা কাকে বলে?"),
                        bi(lang, "Vital Signs Normal Range", "ভাইটাল সাইন স্বাভাবিক মাত্রা"),
                        bi(lang, "Hypertension", "উচ্চ রক্তচাপ"),
                        bi(lang, "Diabetes & DKA", "ডায়াবেটিস ও ইনসুলিন"),
                        bi(lang, "Shock & Sepsis", "শক ও সেপসিস")
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickPrompts.forEach { prompt ->
                            AssistChip(
                                onClick = { viewModel.sendChatQuery(prompt) },
                                label = { Text(prompt, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    FirebaseAiNoticeBanner(language = lang)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 760.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { ClinicalSafetyBanner(language = lang) }

                items(activeMessages, key = { it.id }) { msg ->
                    if (msg.role == "user") {
                        UserChatBubble(message = msg)
                    } else {
                        val precedingQuestion = activeMessages
                            .takeWhile { it.id != msg.id }
                            .lastOrNull { it.role == "user" }?.content ?: "Nursing Query"
                        StructuredAiMessageCard(
                            message = msg,
                            userQuestion = precedingQuestion,
                            language = lang,
                            onSave = { viewModel.saveChatMessage(precedingQuestion, msg) },
                            onFollowUpClick = { followUp -> viewModel.sendChatQuery(followUp) }
                        )
                    }
                }

                if (isGenerating) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = streamingStatus ?: bi(lang, "Asking Gemini Nursing AI...", "Gemini নার্সিং AI উত্তর তৈরি করছে..."),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                chatError?.let { errorState ->
                    item {
                        ChatErrorNoticeCard(
                            errorMessage = errorState.message,
                            language = lang,
                            onRetry = { viewModel.retryLastChatError(useOfflineFallback = false) },
                            onUseOffline = { viewModel.retryLastChatError(useOfflineFallback = true) },
                            onDismiss = { viewModel.dismissChatError() }
                        )
                    }
                }
            }
        }

        if (!voiceStatusMessage.isNullOrBlank() || isListeningVoice) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = voiceStatusMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Bottom ChatGPT-style Search Composer
        Box(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            NursingSearchComposer(
                query = queryText,
                onQueryChange = { queryText = it },
                onSend = {
                    val toSend = queryText
                    queryText = ""
                    viewModel.sendChatQuery(toSend)
                },
                onVoiceClick = ::onVoiceClicked,
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
                        text = bi(lang, "Chat & Search History", "চ্যাট ও সার্চ ইতিহাস"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (searchHistory.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearSearchHistory() }) {
                            Text(bi(lang, "Clear", "মুছুন"))
                        }
                    }
                }
                if (threads.isEmpty() && searchHistory.isEmpty()) {
                    Text(
                        text = bi(lang, "No saved conversations yet.", "এখনো কোনো সার্চ ইতিহাস নেই।"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    threads.take(8).forEach { thread ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectThread(thread.id)
                                    showHistorySheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = thread.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.deleteThread(thread.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete chat")
                                }
                            }
                        }
                    }
                    if (searchHistory.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            text = bi(lang, "Recent Queries", "সাম্প্রতিক প্রশ্ন"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            searchHistory.take(8).forEach { item ->
                                AssistChip(
                                    onClick = {
                                        showHistorySheet = false
                                        viewModel.sendChatQuery(item.query)
                                    },
                                    label = { Text(item.query, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun UserChatBubble(message: ChatMessageEntity) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(max = 560.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!message.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = message.imageUri,
                        contentDescription = "User uploaded study image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(6.dp))
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
private fun ChatErrorNoticeCard(
    errorMessage: String,
    language: AppLanguage,
    onRetry: () -> Unit,
    onUseOffline: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_error_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onRetry) {
                    Text(bi(language, "Retry Gemini AI", "আবার চেষ্টা করুন"))
                }
                TextButton(onClick = onUseOffline) {
                    Text(bi(language, "Offline Reference", "অফলাইন রেফারেন্স"))
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text(bi(language, "Dismiss", "বাদ দিন"))
                }
            }
        }
    }
}

package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.ai.NursingCitation
import com.example.data.ai.NursingSection
import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.data.local.db.ChatMessageEntity
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalAmberContainer
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalGreenContainer
import com.example.ui.theme.OnClinicalAmberContainer
import com.example.ui.theme.OnClinicalGreenContainer
import com.example.ui.viewmodel.NursingViewModel

@Composable
fun FirebaseAiNoticeBanner(
    language: AppLanguage,
    isLiveAi: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isLiveAi) {
            ClinicalGreenContainer.copy(alpha = 0.55f)
        } else {
            ClinicalAmberContainer.copy(alpha = 0.72f)
        },
        border = BorderStroke(
            1.dp,
            if (isLiveAi) ClinicalGreen.copy(alpha = 0.35f) else ClinicalAmber.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isLiveAi) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = "AI Mode",
                tint = if (isLiveAi) ClinicalGreen else OnClinicalAmberContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isLiveAi) {
                    bi(
                        language,
                        "Live Gemini AI Active — Ask any disease, drug, management, indication, or nursing question in English or বাংলা.",
                        "লাইভ Gemini AI সক্রিয় — যেকোনো রোগ, ওষুধ, ম্যানেজমেন্ট, ইন্ডিকেশন বা নার্সিং প্রশ্ন বাংলা বা ইংরেজিতে করুন।"
                    )
                } else {
                    bi(
                        language,
                        "Offline Grounded Reference Mode — Connect to internet for live open-ended Gemini AI responses.",
                        "অফলাইন রেফারেন্স মোড — লাইভ Gemini AI উত্তরের জন্য ইন্টারনেট সংযোগ চালু করুন।"
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isLiveAi) OnClinicalGreenContainer else OnClinicalAmberContainer
            )
        }
    }
}

@Composable
fun ClinicalSafetyBanner(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = "Clinical safety",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = bi(
                    language,
                    "Educational use only. Never replaces independent clinical verification, licensed clinicians, or hospital protocols.",
                    "শুধুমাত্র শিক্ষামূলক ব্যবহারের জন্য। এটি কখনোই চিকিৎসক, সিনিয়র নার্স বা হাসপাতালের প্রোটোকলের বিকল্প নয়।"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun NursingSearchComposer(
    query: String,
    onQueryChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceClick: () -> Unit,
    onImagePickClick: () -> Unit,
    attachedImageUri: Uri?,
    onClearImage: () -> Unit,
    language: AppLanguage,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val canSend = !isLoading && (query.isNotBlank() || attachedImageUri != null)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            if (attachedImageUri != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 6.dp, bottom = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = attachedImageUri,
                        contentDescription = "Attached nursing study image",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = bi(language, "Study image attached", "স্টাডি ছবি সংযুক্ত হয়েছে"),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onClearImage,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove image")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onImagePickClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("image_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = bi(language, "Attach study image", "ছবি সংযুক্ত করুন"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = bi(language, "Ask anything about Nursing...", "নার্সিং সম্পর্কে যেকোনো প্রশ্ন লিখুন..."),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("universal_nursing_search_input"),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )

                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("voice_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = bi(language, "Voice search", "কথা বলে প্রশ্ন করুন"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(4.dp))

                Surface(
                    shape = CircleShape,
                    color = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(enabled = canSend) { onSend() }
                        .testTag("send_search_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = bi(language, "Send question", "প্রশ্ন পাঠান"),
                            tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StructuredAiMessageCard(
    message: ChatMessageEntity,
    userQuestion: String,
    language: AppLanguage,
    onSave: () -> Unit,
    onFollowUpClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val sections = remember(message.sectionsSerialized) {
        NursingViewModel.parseSections(message.sectionsSerialized)
    }
    val citations = remember(message.citationsSerialized) {
        NursingViewModel.parseCitations(message.citationsSerialized)
    }
    val relatedTopics = remember(message.relatedTopicsSerialized) {
        NursingViewModel.parseRelatedTopics(message.relatedTopicsSerialized)
    }
    var savedFeedback by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = if (!message.isSampleNoticeShown) ClinicalGreenContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (!message.isSampleNoticeShown) {
                            bi(language, "NURSING AI • LIVE GEMINI AI", "নার্সিং AI • লাইভ জেমিনি AI")
                        } else {
                            bi(language, "NURSING AI • OFFLINE REFERENCE", "নার্সিং AI • অফলাইন রেফারেন্স")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (!message.isSampleNoticeShown) OnClinicalGreenContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = message.content,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (message.emergencyWarning.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Clinical alert",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = message.emergencyWarning,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            sections.forEachIndexed { idx, section ->
                CollapsibleSectionBlock(
                    section = section,
                    initiallyExpanded = idx < 5
                )
            }

            if (citations.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = bi(language, "Verified References & Sources", "যাচাইকৃত তথ্যসূত্র"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                citations.forEach { cite ->
                    CitationRow(citation = cite)
                }
            }

            if (relatedTopics.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relatedTopics.forEach { topic ->
                        AssistChip(
                            onClick = { onFollowUpClick(topic) },
                            label = { Text(topic, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }

            if (message.isSampleNoticeShown) {
                FirebaseAiNoticeBanner(language = language, isLiveAi = false)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onSave()
                        savedFeedback = true
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (savedFeedback) Icons.Default.CheckCircle else Icons.Default.BookmarkAdd,
                        contentDescription = bi(language, "Save answer", "উত্তর সংরক্ষণ করুন"),
                        tint = if (savedFeedback) ClinicalGreen else MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = {
                        val plain = buildString {
                            appendLine(message.content)
                            sections.forEach { appendLine("\n${it.title}\n${it.body}") }
                        }
                        clipboard.setText(AnnotatedString(plain))
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = bi(language, "Copy answer", "কপি করুন")
                    )
                }
                IconButton(
                    onClick = {
                        val shareText = buildString {
                            appendLine("Nursing AI Reference: $userQuestion")
                            appendLine(message.content)
                            sections.take(4).forEach { appendLine("\n${it.title}\n${it.body}") }
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Nursing Answer"))
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = bi(language, "Share answer", "শেয়ার করুন")
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsibleSectionBlock(
    section: NursingSection,
    initiallyExpanded: Boolean
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse section" else "Expand section",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = section.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun CitationRow(citation: NursingCitation) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ClinicalGreenContainer.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = citation.url.startsWith("http")) {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(citation.url)))
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified source",
                tint = ClinicalGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = citation.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnClinicalGreenContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${citation.verificationStatus} • Updated ${citation.publishedDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun VisualDiagramCard(
    drawableRes: Int,
    title: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

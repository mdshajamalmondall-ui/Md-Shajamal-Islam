package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ai.NursingAiResponse
import com.example.data.ai.SmartAiToolType
import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.ui.components.CitationRow
import com.example.ui.components.ClinicalSafetyBanner
import com.example.ui.components.FirebaseAiNoticeBanner
import com.example.ui.components.VisualDiagramCard
import com.example.ui.viewmodel.AiGenerationState
import com.example.ui.viewmodel.NursingViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: NursingViewModel,
    onAskInMainChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = prefs.language
    val aiState by viewModel.assistantState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var carePlanInput by remember { mutableStateOf("") }
    var selectedTool by remember { mutableStateOf(SmartAiToolType.TUTOR) }
    var smartToolInput by remember { mutableStateOf("") }

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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = bi(lang, "AI Nursing Assistant & Care Plans", "AI নার্সিং অ্যাসিস্ট্যান্ট ও কেয়ার প্ল্যান"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(bi(lang, "Care Plan AI", "কেয়ার প্ল্যান")) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(bi(lang, "Smart Tools", "স্মার্ট টুলস")) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(bi(lang, "Visual Learning", "ভিজ্যুয়াল শিক্ষা")) }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 760.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { FirebaseAiNoticeBanner(language = lang) }

            when (selectedTab) {
                0 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MedicalInformation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = bi(lang, "AI Nursing Care Plan Generator (NANDA-I / NIC / NOC)", "নার্সিং কেয়ার প্ল্যান জেনারেটর (NANDA-I / NIC / NOC)"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = bi(
                                        lang,
                                        "Educational format referencing recognized NANDA-I, NIC, and NOC terminology (subject to institutional licensing).",
                                        "শিক্ষামূলক কাঠামো যা স্বীকৃত NANDA-I, NIC এবং NOC পরিভাষা অনুসরণ করে।"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = carePlanInput,
                                    onValueChange = { carePlanInput = it },
                                    label = { Text(bi(lang, "Enter Patient Case / Vitals / Diagnosis", "রোগীর কেস, ভাইটাল সাইন বা ডায়াগনোসিস লিখুন")) },
                                    placeholder = {
                                        Text(
                                            bi(
                                                lang,
                                                "e.g., 58yo male with pneumonia, SpO2 88%, fever 39°C, productive cough...",
                                                "যেমন: ৫৮ বছর বয়সী নিউমোনিয়া রোগী, SpO2 ৮৮%, জ্বর ৩৯°C, কফযুক্ত কাশি..."
                                            )
                                        )
                                    },
                                    minLines = 3,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("care_plan_input")
                                )

                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        bi(lang, "Pneumonia & Hypoxia", "নিউমোনিয়া ও হাইপোক্সিয়া") to "58yo male with pneumonia, SpO2 88%, RR 26, fever 39.1°C, crackles",
                                        bi(lang, "Severe Pre-eclampsia", "সিভিয়ার প্রি-এক্লাম্পসিয়া") to "28yo pregnant 35 weeks, BP 168/112, severe headache, proteinuria, on MgSO4",
                                        bi(lang, "Hypertensive Crisis", "হাইপারটেনসিভ ক্রাইসিস") to "64yo female BP 194/116 mmHg, occipital headache, chest discomfort"
                                    ).forEach { (label, sample) ->
                                        AssistChip(
                                            onClick = { carePlanInput = sample },
                                            label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.generateCarePlan(carePlanInput) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("generate_care_plan_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(bi(lang, "Generate Nursing Care Plan", "কেয়ার প্ল্যান তৈরি করুন"))
                                }
                            }
                        }
                    }
                    item {
                        AiOutputResultSection(state = aiState, language = lang)
                    }
                }

                1 -> {
                    item {
                        Text(
                            text = bi(lang, "Select a Smart AI Nursing Mode", "স্মার্ট AI নার্সিং টুল বেছে নিন"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmartAiToolType.entries.forEach { tool ->
                                FilterChip(
                                    selected = selectedTool == tool,
                                    onClick = {
                                        selectedTool = tool
                                        smartToolInput = bi(lang, tool.samplePromptEn, tool.samplePromptBn)
                                    },
                                    label = { Text(bi(lang, tool.titleEn, tool.titleBn)) }
                                )
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(bi(lang, selectedTool.titleEn, selectedTool.titleBn), style = MaterialTheme.typography.titleMedium)
                                        Text(bi(lang, selectedTool.subtitleEn, selectedTool.subtitleBn), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                OutlinedTextField(
                                    value = smartToolInput,
                                    onValueChange = { smartToolInput = it },
                                    label = { Text(bi(lang, "Prompt / Study Text", "প্রশ্ন বা নোট লিখুন")) },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = { viewModel.runSmartTool(selectedTool, smartToolInput) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(bi(lang, "Run ${selectedTool.titleEn}", "${selectedTool.titleBn} চালু করুন"))
                                }
                            }
                        }
                    }
                    item {
                        AiOutputResultSection(state = aiState, language = lang)
                    }
                }

                2 -> {
                    item {
                        Text(
                            text = bi(lang, "Educational Medical & Anatomy Visuals", "মেডিকেল ও নার্সিং ভিজ্যুয়াল শিক্ষা"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = bi(
                                lang,
                                "All diagrams are labeled educational illustrations. Tap any diagram to open its structured study guide in Nursing AI.",
                                "সব চিত্র শিক্ষামূলক ইলাস্ট্রেশন হিসেবে চিহ্নিত। বিস্তারিত পড়তে যেকোনো চিত্রে ট্যাপ করুন।"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        VisualDiagramCard(
                            drawableRes = R.drawable.img_anatomy_heart,
                            title = bi(lang, "Human Heart Anatomy & Conduction", "হৃৎপিণ্ডের গঠন ও বৈদ্যুতিক কন্ডাকশন"),
                            subtitle = bi(lang, "Chambers, valves, SA/AV node pathway & oxygenated blood flow", "প্রকোষ্ঠ, ভালভ, SA/AV নোড ও রক্ত সঞ্চালন"),
                            badge = bi(lang, "AI-Generated Educational Diagram", "শিক্ষামূলক চিত্র"),
                            onClick = { onAskInMainChat("Explain Cardiac Cycle, SA/AV Node, and heart valves") }
                        )
                    }
                    item {
                        VisualDiagramCard(
                            drawableRes = R.drawable.img_anatomy_respiratory,
                            title = bi(lang, "Respiratory System & Gas Exchange", "শ্বাসতন্ত্র ও গ্যাস বিনিময়"),
                            subtitle = bi(lang, "Trachea, bronchi, alveoli, diaphragm & COPD/Pneumonia care", "ট্রাকিয়া, ব্রঙ্কাই, অ্যালভিওলাই ও শ্বাসতন্ত্রের যত্ন"),
                            badge = bi(lang, "AI-Generated Educational Diagram", "শিক্ষামূলক চিত্র"),
                            onClick = { onAskInMainChat("Explain Pneumonia, alveoli gas exchange, and respiratory nursing care") }
                        )
                    }
                    item {
                        VisualDiagramCard(
                            drawableRes = R.drawable.img_procedure_iv,
                            title = bi(lang, "Peripheral IV Cannulation Technique", "পেরিফেরাল IV ক্যানুলেশন পদ্ধতি"),
                            subtitle = bi(lang, "15–30° insertion angle, aseptic prep, flashback & securement", "১৫–৩০° কোণ, জীবাণুমুক্ত প্রস্তুতি ও ক্যানুলা স্থাপন"),
                            badge = bi(lang, "Procedure Illustration • Supervision Req.", "প্রসিডিউর চিত্র"),
                            onClick = { onAskInMainChat("Step-by-step Peripheral IV Cannulation procedure") }
                        )
                    }
                }
            }

            item { ClinicalSafetyBanner(language = lang) }
        }
    }
}

@Composable
private fun AiOutputResultSection(state: AiGenerationState, language: AppLanguage) {
    when (state) {
        AiGenerationState.Idle -> Unit
        AiGenerationState.Loading -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.width(12.dp))
                Text(bi(language, "Preparing structured nursing output...", "নার্সিং ফলাফল তৈরি হচ্ছে..."))
            }
        }
        is AiGenerationState.Error -> {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
        is AiGenerationState.Success -> {
            StructuredResponsePreviewCard(response = state.response, language = language)
        }
    }
}

@Composable
private fun StructuredResponsePreviewCard(response: NursingAiResponse, language: AppLanguage) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(response.summary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (response.emergencyWarning.isNotBlank()) {
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.errorContainer) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(response.emergencyWarning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            response.sections.forEach { sec ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(sec.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(sec.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            response.citations.forEach { CitationRow(it) }
            Text(
                text = response.firebaseNotice,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.bi
import com.example.ui.viewmodel.NursingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    viewModel: NursingViewModel,
    onAskInMainChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = prefs.language
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
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = bi(lang, "Nursing Tools & Clinical Suite", "নার্সিং টুলস ও ক্লিনিক্যাল স্যুট"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(bi(lang, "Calculators (12)", "ক্যালকুলেটর (১২)")) },
                        modifier = Modifier.testTag("tools_tab_calculators")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(bi(lang, "Care Plan & AI", "কেয়ার প্ল্যান ও AI")) },
                        modifier = Modifier.testTag("tools_tab_ai")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(bi(lang, "Drug Guide", "ওষুধের তথ্য")) },
                        modifier = Modifier.testTag("tools_tab_drugs")
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text(bi(lang, "Procedures", "প্রসিডিউর")) },
                        modifier = Modifier.testTag("tools_tab_procedures")
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text(bi(lang, "MCQ & Flashcards", "MCQ ও ফ্ল্যাশকার্ড")) },
                        modifier = Modifier.testTag("tools_tab_mcq")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> ClinicalToolsScreen(viewModel = viewModel, modifier = Modifier.weight(1f))
            1 -> AiAssistantScreen(
                viewModel = viewModel,
                onAskInMainChat = onAskInMainChat,
                modifier = Modifier.weight(1f)
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
            4 -> StudentZoneTab(
                lang = lang,
                mcqs = mcqs,
                completedToday = prefs.completedTodayQuestions,
                dailyGoal = prefs.dailyGoalQuestions,
                streak = prefs.studyStreakDays,
                onRecordAnswer = viewModel::recordMcqAnswer
            )
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppLanguage
import com.example.data.local.bi
import com.example.domain.CalcResult
import com.example.domain.ClinicalCalculators
import com.example.ui.components.ClinicalSafetyBanner
import com.example.ui.viewmodel.NursingViewModel

enum class ClinicalToolId(val labelEn: String, val labelBn: String) {
    IV_FLOW("IV Flow Rate", "IV ফ্লো রেট"),
    DOSAGE("Dosage Calc", "ওষুধের ডোজ"),
    PEDIATRIC("Pediatric Dose", "শিশুদের ডোজ"),
    BMI("BMI", "বিএমআই (BMI)"),
    BSA("BSA", "বিএসএ (BSA)"),
    DRIP_TIME("IV Drip Time", "IV সময়"),
    FLUID_BALANCE("I&O Fluid Chart", "ফ্লুইড ব্যালেন্স"),
    GCS("Glasgow Coma Scale", "GCS স্কোর"),
    APGAR("APGAR Score", "APGAR স্কোর"),
    TEMP("Temp Converter", "তাপমাত্রা"),
    WEIGHT("Weight Converter", "ওজন রূপান্তর"),
    VITALS_TEMPLATE("Vitals & SBAR", "ভাইটাল ও SBAR")
}

@Composable
fun ClinicalToolsScreen(
    viewModel: NursingViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val lang = prefs.language
    var selectedTool by remember { mutableStateOf(ClinicalToolId.IV_FLOW) }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = bi(lang, "Clinical Nursing Tools & Calculators", "ক্লিনিক্যাল নার্সিং টুলস ও ক্যালকুলেটর"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ClinicalToolId.entries) { tool ->
                        FilterChip(
                            selected = selectedTool == tool,
                            onClick = { selectedTool = tool },
                            label = { Text(bi(lang, tool.labelEn, tool.labelBn)) },
                            modifier = Modifier.testTag("tool_chip_${tool.name.lowercase()}")
                        )
                    }
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
            item { ClinicalSafetyBanner(language = lang) }
            item {
                when (selectedTool) {
                    ClinicalToolId.IV_FLOW -> IvFlowToolCard(lang)
                    ClinicalToolId.DOSAGE -> DosageToolCard(lang)
                    ClinicalToolId.PEDIATRIC -> PediatricDoseToolCard(lang)
                    ClinicalToolId.BMI -> BmiToolCard(lang)
                    ClinicalToolId.BSA -> BsaToolCard(lang)
                    ClinicalToolId.DRIP_TIME -> IvDripTimeToolCard(lang)
                    ClinicalToolId.FLUID_BALANCE -> FluidBalanceToolCard(lang)
                    ClinicalToolId.GCS -> GcsToolCard(lang)
                    ClinicalToolId.APGAR -> ApgarToolCard(lang)
                    ClinicalToolId.TEMP -> TempConverterToolCard(lang)
                    ClinicalToolId.WEIGHT -> WeightConverterToolCard(lang)
                    ClinicalToolId.VITALS_TEMPLATE -> VitalsAndAssessmentCard(lang)
                }
            }
        }
    }
}

@Composable
private fun CalculatorCardFrame(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun CalcResultBox(result: CalcResult?) {
    if (result == null) return
    when (result) {
        is CalcResult.ValidationError -> {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                Text(result.message, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.fillMaxWidth().padding(12.dp))
            }
        }
        is CalcResult.Success -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth().testTag("calc_result_box")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(result.primaryValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Formula: ${result.formulaUsed}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(result.interpretation, style = MaterialTheme.typography.bodyMedium)
                    if (!result.safetyWarning.isNullOrBlank()) {
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.errorContainer) {
                            Row(modifier = Modifier.padding(10.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text(result.safetyWarning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun IvFlowToolCard(lang: AppLanguage) {
    var vol by remember { mutableStateOf("1000") }
    var hrs by remember { mutableStateOf("8") }
    var dropFactor by remember { mutableIntStateOf(20) }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    CalculatorCardFrame(
        title = bi(lang, "IV Flow Rate Calculator", "IV ফ্লো রেট ক্যালকুলেটর"),
        subtitle = bi(lang, "Calculates pump mL/hr & gravity gtt/min", "পাম্প mL/hr ও গ্র্যাভিটি ড্রিপ gtt/min হিসাব")
    ) {
        NumberField(vol, { vol = it }, bi(lang, "Total Volume (mL)", "মোট আয়তন (mL)"))
        NumberField(hrs, { hrs = it }, bi(lang, "Infusion Duration (Hours)", "সময়কাল (ঘণ্টা)"))
        Text(bi(lang, "Drop Factor (gtt/mL)", "ড্রপ ফ্যাক্টর (gtt/mL)"), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(10, 15, 20, 60).forEach { df ->
                FilterChip(selected = dropFactor == df, onClick = { dropFactor = df }, label = { Text("$df") })
            }
        }
        Button(onClick = { result = ClinicalCalculators.calculateIvFlowRate(vol.toDoubleOrNull(), hrs.toDoubleOrNull(), dropFactor) }, modifier = Modifier.fillMaxWidth()) {
            Text(bi(lang, "Calculate Flow Rate", "ফ্লো রেট হিসাব করুন"))
        }
        CalcResultBox(result)
    }
}

@Composable
private fun DosageToolCard(lang: AppLanguage) {
    var desired by remember { mutableStateOf("500") }
    var have by remember { mutableStateOf("250") }
    var vehicle by remember { mutableStateOf("5") }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    CalculatorCardFrame(
        title = bi(lang, "Medication Dosage Calculator", "ওষুধের ডোজ ক্যালকুলেটর"),
        subtitle = bi(lang, "Formula: (Desired / Have) × Vehicle", "সূত্র: (Desired / Have) × Vehicle")
    ) {
        NumberField(desired, { desired = it }, bi(lang, "Desired Dose (e.g., mg)", "কাঙ্ক্ষিত ডোজ (mg/mcg)"))
        NumberField(have, { have = it }, bi(lang, "Available Strength (same unit)", "উপলব্ধ মাত্রা (একই একক)"))
        NumberField(vehicle, { vehicle = it }, bi(lang, "Vehicle Volume (mL or 1 tab)", "ভলিউম (mL বা ১ ট্যাবলেট)"))
        Button(onClick = { result = ClinicalCalculators.calculateDosage(desired.toDoubleOrNull(), have.toDoubleOrNull(), vehicle.toDoubleOrNull()) }, modifier = Modifier.fillMaxWidth()) {
            Text(bi(lang, "Calculate Dose", "ডোজ হিসাব করুন"))
        }
        CalcResultBox(result)
    }
}

@Composable
private fun PediatricDoseToolCard(lang: AppLanguage) {
    var weight by remember { mutableStateOf("14") }
    var mgPerKg by remember { mutableStateOf("50") }
    var freq by remember { mutableIntStateOf(3) }
    var maxDaily by remember { mutableStateOf("1500") }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    CalculatorCardFrame(
        title = bi(lang, "Pediatric Weight-Based Dose Support", "শিশুদের ওজন-ভিত্তিক ডোজ সহায়ক"),
        subtitle = bi(lang, "Calculates mg/day & per-dose with safety cap check", "দৈনিক ও প্রতি ডোজের পরিমাণ")
    ) {
        NumberField(weight, { weight = it }, bi(lang, "Child Weight (kg)", "শিশুর ওজন (kg)"))
        NumberField(mgPerKg, { mgPerKg = it }, bi(lang, "Ordered Dose (mg/kg/day)", "নির্ধারিত মাত্রা (mg/kg/day)"))
        NumberField(maxDaily, { maxDaily = it }, bi(lang, "Max Daily Ceiling (mg, optional)", "সর্বোচ্চ দৈনিক সীমা (mg)"))
        Text(bi(lang, "Doses per 24h", "দিনে কয়বার"), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 4).forEach { f ->
                FilterChip(selected = freq == f, onClick = { freq = f }, label = { Text("${f}x/day") })
            }
        }
        Button(onClick = { result = ClinicalCalculators.calculatePediatricDose(weight.toDoubleOrNull(), mgPerKg.toDoubleOrNull(), freq, maxDaily.toDoubleOrNull()) }, modifier = Modifier.fillMaxWidth()) {
            Text(bi(lang, "Calculate Pediatric Dose", "শিশুর ডোজ হিসাব করুন"))
        }
        CalcResultBox(result)
    }
}

@Composable
private fun BmiToolCard(lang: AppLanguage) {
    var weight by remember { mutableStateOf("64") }
    var height by remember { mutableStateOf("165") }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    CalculatorCardFrame(
        title = bi(lang, "BMI Calculator", "বিএমআই (BMI) ক্যালকুলেটর"),
        subtitle = bi(lang, "WHO & South Asian Adult Classifications", "WHO ও সাউথ এশিয়ান মানদণ্ড")
    ) {
        NumberField(weight, { weight = it }, bi(lang, "Weight (kg)", "ওজন (কেজি)"))
        NumberField(height, { height = it }, bi(lang, "Height (cm)", "উচ্চতা (সেমি)"))
        Button(onClick = { result = ClinicalCalculators.calculateBmi(weight.toDoubleOrNull(), height.toDoubleOrNull()) }, modifier = Modifier.fillMaxWidth()) {
            Text(bi(lang, "Calculate BMI", "BMI নির্ণয় করুন"))
        }
        CalcResultBox(result)
    }
}

@Composable
private fun BsaToolCard(lang: AppLanguage) {
    var weight by remember { mutableStateOf("65") }
    var height by remember { mutableStateOf("170") }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    CalculatorCardFrame(
        title = bi(lang, "Body Surface Area (BSA — Mosteller)", "বডি সারফেস এরিয়া (BSA)"),
        subtitle = bi(lang, "Used in oncology, renal & pediatric dosing", "অনকোলজি ও পেডিয়াট্রিক ডোজিংয়ে ব্যবহৃত")
    ) {
        NumberField(weight, { weight = it }, bi(lang, "Weight (kg)", "ওজন (kg)"))
        NumberField(height, { height = it }, bi(lang, "Height (cm)", "উচ্চতা (cm)"))
        Button(onClick = { result = ClinicalCalculators.calculateBsa(weight.toDoubleOrNull(), height.toDoubleOrNull()) }, modifier = Modifier.fillMaxWidth()) {
            Text(bi(lang, "Calculate BSA", "BSA নির্ণয় করুন"))
        }
        CalcResultBox(result)
    }
}

@Composable
private fun IvDripTimeToolCard(lang: AppLanguage) {
    var vol by remember { mutableStateOf("500") }
    var rate by remember { mutableStateOf("125") }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    CalculatorCardFrame(
        title = bi(lang, "IV Drip Completion Time", "IV ড্রিপ শেষ হওয়ার সময়"),
        subtitle = bi(lang, "Estimate infusion duration from mL and mL/hr", "স্যালাইন চলার সময়কাল হিসাব")
    ) {
        NumberField(vol, { vol = it }, bi(lang, "Remaining Volume (mL)", "বাকি তরল (mL)"))
        NumberField(rate, { rate = it }, bi(lang, "Infusion Rate (mL/hr)", "হার (mL/hr)"))
        Button(onClick = { result = ClinicalCalculators.calculateIvDripDuration(vol.toDoubleOrNull(), rate.toDoubleOrNull()) }, modifier = Modifier.fillMaxWidth()) {
            Text(bi(lang, "Calculate Duration", "সময় নির্ণয় করুন"))
        }
        CalcResultBox(result)
    }
}

@Composable
private fun FluidBalanceToolCard(lang: AppLanguage) {
    var oral by remember { mutableStateOf("800") }
    var iv by remember { mutableStateOf("1000") }
    var otherIn by remember { mutableStateOf("0") }
    var urine by remember { mutableStateOf("1450") }
    var drain by remember { mutableStateOf("100") }
    var stool by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    CalculatorCardFrame(
        title = bi(lang, "24-Hour Intake & Output Chart", "২৪ ঘণ্টার ইনটেক ও আউটপুট চার্ট"),
        subtitle = bi(lang, "Net fluid balance monitoring (mL)", "নেট ফ্লুইড ব্যালেন্স হিসাব")
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(oral, { oral = it }, "Oral In (mL)", Modifier.weight(1f))
            NumberField(iv, { iv = it }, "IV In (mL)", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(otherIn, { otherIn = it }, "Tube/Other In", Modifier.weight(1f))
            NumberField(urine, { urine = it }, "Urine Out (mL)", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(drain, { drain = it }, "Drain/Emesis", Modifier.weight(1f))
            NumberField(stool, { stool = it }, "Other Out (mL)", Modifier.weight(1f))
        }
        Button(
            onClick = {
                result = ClinicalCalculators.calculateFluidBalance(
                    oral.toDoubleOrNull() ?: 0.0,
                    iv.toDoubleOrNull() ?: 0.0,
                    otherIn.toDoubleOrNull() ?: 0.0,
                    urine.toDoubleOrNull() ?: 0.0,
                    drain.toDoubleOrNull() ?: 0.0,
                    stool.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(bi(lang, "Compute Net Balance", "নেট ব্যালেন্স হিসাব করুন"))
        }
        CalcResultBox(result)
    }
}

@Composable
private fun GcsToolCard(lang: AppLanguage) {
    var eye by remember { mutableIntStateOf(4) }
    var verbal by remember { mutableIntStateOf(5) }
    var motor by remember { mutableIntStateOf(6) }
    val result = ClinicalCalculators.evaluateGcs(eye, verbal, motor)

    CalculatorCardFrame(
        title = bi(lang, "Glasgow Coma Scale (GCS)", "গ্লাসগো কোমা স্কেল (GCS)"),
        subtitle = bi(lang, "Eye (1–4) + Verbal (1–5) + Motor (1–6)", "স্নায়বিক চেতনা মূল্যায়ন")
    ) {
        Text("Eye Opening (E1–E4)", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1 to "1 None", 2 to "2 Pain", 3 to "3 Voice", 4 to "4 Spont").forEach { (v, l) ->
                FilterChip(selected = eye == v, onClick = { eye = v }, label = { Text(l) })
            }
        }
        Text("Verbal Response (V1–V5)", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1 to "1", 2 to "2 Sounds", 3 to "3 Words", 4 to "4 Confused", 5 to "5 Oriented").forEach { (v, l) ->
                FilterChip(selected = verbal == v, onClick = { verbal = v }, label = { Text(l) })
            }
        }
        Text("Motor Response (M1–M6)", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..6).forEach { m ->
                FilterChip(selected = motor == m, onClick = { motor = m }, label = { Text("M$m") })
            }
        }
        CalcResultBox(result)
    }
}

@Composable
private fun ApgarToolCard(lang: AppLanguage) {
    var a by remember { mutableIntStateOf(2) }
    var p by remember { mutableIntStateOf(2) }
    var g by remember { mutableIntStateOf(2) }
    var act by remember { mutableIntStateOf(2) }
    var r by remember { mutableIntStateOf(2) }

    CalculatorCardFrame(
        title = bi(lang, "APGAR Score Reference & Calculator", "APGAR স্কোর ক্যালকুলেটর"),
        subtitle = bi(lang, "Newborn assessment at 1 & 5 minutes", "নবজাতকের ১ ও ৫ মিনিটের মূল্যায়ন")
    ) {
        ApgarRow("Appearance (Color)", a) { a = it }
        ApgarRow("Pulse (HR >100=2)", p) { p = it }
        ApgarRow("Grimace (Reflex)", g) { g = it }
        ApgarRow("Activity (Tone)", act) { act = it }
        ApgarRow("Respiration (Cry)", r) { r = it }
        CalcResultBox(ClinicalCalculators.evaluateApgar(a, p, g, act, r))
    }
}

@Composable
private fun ApgarRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0..2).forEach { score ->
                FilterChip(selected = value == score, onClick = { onChange(score) }, label = { Text("$score") })
            }
        }
    }
}

@Composable
private fun TempConverterToolCard(lang: AppLanguage) {
    var input by remember { mutableStateOf("37.5") }
    var fromC by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    CalculatorCardFrame(
        title = bi(lang, "Temperature Converter", "তাপমাত্রা রূপান্তরকারী"),
        subtitle = bi(lang, "Celsius (°C) ↔ Fahrenheit (°F) ↔ Kelvin", "সেলসিয়াস ও ফারেনহাইট রূপান্তর")
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = fromC, onClick = { fromC = true }, label = { Text("°C → °F") })
            FilterChip(selected = !fromC, onClick = { fromC = false }, label = { Text("°F → °C") })
        }
        NumberField(input, { input = it }, if (fromC) "Temperature (°C)" else "Temperature (°F)")
        Button(onClick = { result = ClinicalCalculators.convertTemperature(input.toDoubleOrNull(), fromC) }, modifier = Modifier.fillMaxWidth()) {
            Text(bi(lang, "Convert Temperature", "রূপান্তর করুন"))
        }
        CalcResultBox(result)
    }
}

@Composable
private fun WeightConverterToolCard(lang: AppLanguage) {
    var input by remember { mutableStateOf("60") }
    var kgToLb by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<CalcResult?>(null) }

    CalculatorCardFrame(
        title = bi(lang, "Weight Converter (kg ↔ lb)", "ওজন রূপান্তরকারী (kg ↔ lb)"),
        subtitle = bi(lang, "Supports safe mg/kg dosing", "নিরাপদ mg/kg ডোজিংয়ের জন্য")
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = kgToLb, onClick = { kgToLb = true }, label = { Text("kg → lb") })
            FilterChip(selected = !kgToLb, onClick = { kgToLb = false }, label = { Text("lb → kg") })
        }
        NumberField(input, { input = it }, if (kgToLb) "Weight (kg)" else "Weight (lb)")
        Button(onClick = { result = ClinicalCalculators.convertWeight(input.toDoubleOrNull(), kgToLb) }, modifier = Modifier.fillMaxWidth()) {
            Text(bi(lang, "Convert Weight", "রূপান্তর করুন"))
        }
        CalcResultBox(result)
    }
}

@Composable
private fun VitalsAndAssessmentCard(lang: AppLanguage) {
    CalculatorCardFrame(
        title = bi(lang, "Vital Signs Reference & Assessment Templates", "ভাইটাল সাইন রেফারেন্স ও অ্যাসেসমেন্ট টেমপ্লেট"),
        subtitle = bi(lang, "Normal ranges by age + SBAR handover template", "বয়সভিত্তিক স্বাভাবিক মান ও SBAR টেমপ্লেট")
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) {
            Text(
                text = "Normal Vital Signs Reference:\n" +
                    "• Adult: HR 60–100 bpm | RR 12–20/min | BP <120/80 mmHg | Temp 36.5–37.5°C | SpO2 95–100%\n" +
                    "• Child (1–12 yr): HR 70–120 bpm | RR 18–30/min | SBP >= 70 + (2×age)\n" +
                    "• Neonate: HR 110–160 bpm | RR 30–60/min | SBP 60–80 mmHg",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)) {
            Text(
                text = "SBAR Clinical Handover Template:\n" +
                    "• S (Situation): Patient name, unit, primary concern/vitals.\n" +
                    "• B (Background): Admission diagnosis, allergies, relevant meds/labs.\n" +
                    "• A (Assessment): Current ABCDE findings, pain, I&O, neuro status.\n" +
                    "• R (Recommendation): Specific clinical request (orders, stat labs, escalation).",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

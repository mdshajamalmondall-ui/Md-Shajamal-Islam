package com.example.domain

import kotlin.math.roundToInt
import kotlin.math.sqrt

sealed class CalcResult {
    data class Success(
        val primaryValue: String,
        val formulaUsed: String,
        val interpretation: String,
        val safetyWarning: String? = null
    ) : CalcResult()

    data class ValidationError(val message: String) : CalcResult()
}

object ClinicalCalculators {

    fun calculateBmi(weightKg: Double?, heightCm: Double?): CalcResult {
        if (weightKg == null || heightCm == null || weightKg <= 0 || heightCm <= 0) {
            return CalcResult.ValidationError("Enter positive weight (kg) and height (cm).")
        }
        if (weightKg > 400 || heightCm < 30 || heightCm > 260) {
            return CalcResult.ValidationError("Verify realistic anthropometric values (height 30–260 cm, weight <= 400 kg).")
        }
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        val rounded = (bmi * 10.0).roundToInt() / 10.0
        val category = when {
            rounded < 18.5 -> "Underweight (WHO < 18.5)"
            rounded < 25.0 -> "Normal Weight (WHO 18.5–24.9; South Asian target 18.5–22.9)"
            rounded < 30.0 -> "Overweight (WHO 25.0–29.9)"
            rounded < 35.0 -> "Obesity Class I (30.0–34.9)"
            rounded < 40.0 -> "Obesity Class II (35.0–39.9)"
            else -> "Obesity Class III (>= 40.0)"
        }
        return CalcResult.Success(
            primaryValue = "$rounded kg/m²",
            formulaUsed = "BMI = Weight (kg) / [Height (m)]² = $weightKg / (${(heightM * 100).roundToInt() / 100.0})²",
            interpretation = category
        )
    }

    fun calculateBsa(weightKg: Double?, heightCm: Double?): CalcResult {
        if (weightKg == null || heightCm == null || weightKg <= 0 || heightCm <= 0) {
            return CalcResult.ValidationError("Enter positive weight (kg) and height (cm).")
        }
        val bsa = sqrt((heightCm * weightKg) / 3600.0)
        val rounded = (bsa * 100.0).roundToInt() / 100.0
        return CalcResult.Success(
            primaryValue = "$rounded m²",
            formulaUsed = "Mosteller BSA = √[(Height cm × Weight kg) / 3600]",
            interpretation = "Typical adult BSA is 1.6–2.0 m². Used for chemotherapy, pediatric critical dosing, and cardiac index.",
            safetyWarning = "Medication Safety: BSA-based chemotherapy and pediatric doses require independent double-check by two licensed clinicians."
        )
    }

    fun calculateIvFlowRate(volumeMl: Double?, hours: Double?, dropFactor: Int): CalcResult {
        if (volumeMl == null || hours == null || volumeMl <= 0 || hours <= 0) {
            return CalcResult.ValidationError("Enter positive volume (mL) and duration (hours).")
        }
        if (dropFactor !in listOf(10, 15, 20, 60)) {
            return CalcResult.ValidationError("Drop factor must be 10, 15, 20 (macrodrip) or 60 (microdrip) gtt/mL.")
        }
        val mlPerHour = volumeMl / hours
        val gttPerMin = (volumeMl * dropFactor) / (hours * 60.0)
        return CalcResult.Success(
            primaryValue = "${mlPerHour.roundToInt()} mL/hr  •  ${gttPerMin.roundToInt()} gtt/min",
            formulaUsed = "mL/hr = Volume / Hours | gtt/min = (Volume mL × Drop Factor $dropFactor) / (Hours × 60)",
            interpretation = "Exact pump rate: ${(mlPerHour * 10).roundToInt() / 10.0} mL/hr. Gravity drip count: ${gttPerMin.roundToInt()} drops/min.",
            safetyWarning = "Verify tubing drop factor on package label and monitor IV site for infiltration/fluid overload."
        )
    }

    fun calculateIvDripDuration(volumeMl: Double?, rateMlPerHr: Double?): CalcResult {
        if (volumeMl == null || rateMlPerHr == null || volumeMl <= 0 || rateMlPerHr <= 0) {
            return CalcResult.ValidationError("Enter positive volume (mL) and infusion rate (mL/hr).")
        }
        val totalHours = volumeMl / rateMlPerHr
        val wholeHours = totalHours.toInt()
        val minutes = ((totalHours - wholeHours) * 60).roundToInt()
        return CalcResult.Success(
            primaryValue = "${wholeHours}h ${minutes}m",
            formulaUsed = "Duration (hr) = Volume (mL) / Rate (mL/hr) = $volumeMl / $rateMlPerHr",
            interpretation = "Infusion will complete in approximately $wholeHours hours and $minutes minutes."
        )
    }

    fun calculateDosage(desired: Double?, have: Double?, vehicleQty: Double?): CalcResult {
        if (desired == null || have == null || vehicleQty == null || desired <= 0 || have <= 0 || vehicleQty <= 0) {
            return CalcResult.ValidationError("Desired dose, available strength, and vehicle volume/quantity must all be > 0.")
        }
        val result = (desired / have) * vehicleQty
        val rounded = (result * 100.0).roundToInt() / 100.0
        return CalcResult.Success(
            primaryValue = "$rounded unit(s) / mL",
            formulaUsed = "Dose = (Desired / Have) × Vehicle = ($desired / $have) × $vehicleQty",
            interpretation = "Administer $rounded mL (or tablets/capsules matching your vehicle unit). Ensure Desired and Have share identical units (e.g., both mg or both mcg).",
            safetyWarning = "CLINICAL SAFETY WARNING: Never rely solely on an app calculator. Perform unit-conversion checks and independent double verification for High-Alert drugs."
        )
    }

    fun calculatePediatricDose(
        weightKg: Double?,
        mgPerKgPerDay: Double?,
        dosesPerDay: Int,
        maxDailyMg: Double? = null
    ): CalcResult {
        if (weightKg == null || mgPerKgPerDay == null || weightKg <= 0 || mgPerKgPerDay <= 0 || dosesPerDay <= 0) {
            return CalcResult.ValidationError("Enter positive child weight (kg), mg/kg/day, and frequency.")
        }
        val totalDaily = weightKg * mgPerKgPerDay
        val cappedDaily = if (maxDailyMg != null && maxDailyMg > 0) minOf(totalDaily, maxDailyMg) else totalDaily
        val singleDose = cappedDaily / dosesPerDay
        val wasCapped = maxDailyMg != null && maxDailyMg > 0 && totalDaily > maxDailyMg
        val dailyRound = (cappedDaily * 10.0).roundToInt() / 10.0
        val singleRound = (singleDose * 10.0).roundToInt() / 10.0
        return CalcResult.Success(
            primaryValue = "$singleRound mg / dose  ($dailyRound mg/day)",
            formulaUsed = "Total Daily = $weightKg kg × $mgPerKgPerDay mg/kg/day | Single Dose = Daily / $dosesPerDay",
            interpretation = if (wasCapped) {
                "Calculated daily dose (${(totalDaily * 10).roundToInt() / 10.0} mg) exceeded adult/max ceiling ($maxDailyMg mg) and was capped."
            } else {
                "Divide into $dosesPerDay equal doses every ${24 / dosesPerDay} hours."
            },
            safetyWarning = "PEDIATRIC SAFETY ALERT: Always verify current patient weight in kg (not lb) and confirm safe pediatric range in BNFc/institutional formulary."
        )
    }

    fun calculateFluidBalance(
        oralMl: Double,
        ivMl: Double,
        otherInMl: Double,
        urineMl: Double,
        emesisDrainMl: Double,
        stoolOtherOutMl: Double
    ): CalcResult {
        val totalIn = oralMl + ivMl + otherInMl
        val totalOut = urineMl + emesisDrainMl + stoolOtherOutMl
        val net = totalIn - totalOut
        val sign = if (net >= 0) "+" else ""
        val status = when {
            net > 1000 -> "Positive fluid balance (+${net.roundToInt()} mL) — assess for edema, crackles, or overload."
            net < -500 -> "Negative fluid balance (${net.roundToInt()} mL) — monitor hydration, BP, and urine output (>=0.5 mL/kg/hr)."
            else -> "Balanced 24-hour intake & output."
        }
        return CalcResult.Success(
            primaryValue = "In: ${totalIn.roundToInt()} mL | Out: ${totalOut.roundToInt()} mL | Net: $sign${net.roundToInt()} mL",
            formulaUsed = "Net Balance = Total Intake (${totalIn.roundToInt()}) − Total Output (${totalOut.roundToInt()})",
            interpretation = status
        )
    }

    fun convertTemperature(value: Double?, fromCelsius: Boolean): CalcResult {
        if (value == null) return CalcResult.ValidationError("Enter a valid temperature.")
        return if (fromCelsius) {
            val f = (value * 9.0 / 5.0) + 32.0
            val k = value + 273.15
            val fRound = (f * 10.0).roundToInt() / 10.0
            val kRound = (k * 10.0).roundToInt() / 10.0
            val note = when {
                value >= 38.0 -> "Febrile (>= 38.0°C / 100.4°F)"
                value < 36.0 -> "Hypothermia (< 36.0°C / 96.8°F)"
                else -> "Normothermic (36.5–37.5°C)"
            }
            CalcResult.Success("$fRound °F  •  $kRound K", "(°C × 9/5) + 32 = °F", note)
        } else {
            val c = (value - 32.0) * 5.0 / 9.0
            val k = c + 273.15
            val cRound = (c * 10.0).roundToInt() / 10.0
            val kRound = (k * 10.0).roundToInt() / 10.0
            val note = when {
                cRound >= 38.0 -> "Febrile (>= 38.0°C / 100.4°F)"
                cRound < 36.0 -> "Hypothermia (< 36.0°C / 96.8°F)"
                else -> "Normothermic (97.7–99.5°F)"
            }
            CalcResult.Success("$cRound °C  •  $kRound K", "(°F − 32) × 5/9 = °C", note)
        }
    }

    fun convertWeight(value: Double?, kgToLb: Boolean): CalcResult {
        if (value == null || value <= 0) return CalcResult.ValidationError("Enter a positive weight.")
        return if (kgToLb) {
            val lb = (value * 2.20462 * 100.0).roundToInt() / 100.0
            CalcResult.Success("$lb lb", "lb = kg × 2.20462", "Always document weight in kilograms (kg) for medication safety.")
        } else {
            val kg = (value / 2.20462 * 100.0).roundToInt() / 100.0
            CalcResult.Success("$kg kg", "kg = lb / 2.20462", "Use $kg kg for all weight-based mg/kg calculations.")
        }
    }

    fun evaluateGcs(eye: Int, verbal: Int, motor: Int): CalcResult {
        val total = eye + verbal + motor
        val severity = when {
            total >= 13 -> "Mild Brain Injury / Intact (GCS 13–15)"
            total >= 9 -> "Moderate Impairment (GCS 9–12) — close neuro monitoring"
            else -> "Severe Coma / Critical (GCS <= 8) — 'GCS 8 or less, intubate!'"
        }
        return CalcResult.Success(
            primaryValue = "GCS $total / 15  (E$eye V$verbal M$motor)",
            formulaUsed = "Total GCS = Eye ($eye/4) + Verbal ($verbal/5) + Motor ($motor/6)",
            interpretation = severity,
            safetyWarning = if (total <= 8) "CRITICAL ALERT: GCS <= 8 indicates inability to protect airway. Escalate immediately for airway management." else null
        )
    }

    fun evaluateApgar(appearance: Int, pulse: Int, grimace: Int, activity: Int, respiration: Int): CalcResult {
        val total = appearance + pulse + grimace + activity + respiration
        val interpretation = when {
            total >= 7 -> "Reassuring / Good Transition (7–10) — routine newborn care & skin-to-skin."
            total >= 4 -> "Moderately Abnormal (4–6) — stimulate, clear airway if needed, give O2/supportive resuscitation."
            else -> "Critical / Severe Depression (0–3) — immediate neonatal resuscitation (NRP protocol)!"
        }
        return CalcResult.Success(
            primaryValue = "APGAR $total / 10",
            formulaUsed = "A($appearance) + P($pulse) + G($grimace) + A($activity) + R($respiration)",
            interpretation = interpretation,
            safetyWarning = if (total < 7) "Note: Never delay neonatal resuscitation waiting for the 1-minute APGAR score!" else null
        )
    }
}

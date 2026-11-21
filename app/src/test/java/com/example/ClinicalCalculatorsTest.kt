package com.example

import com.example.domain.CalcResult
import com.example.domain.ClinicalCalculators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalCalculatorsTest {

    @Test
    fun `iv flow rate computes ml per hr and drops per min accurately`() {
        val result = ClinicalCalculators.calculateIvFlowRate(1000.0, 8.0, 20)
        assertTrue(result is CalcResult.Success)
        val ok = result as CalcResult.Success
        assertTrue(ok.primaryValue.contains("125 mL/hr"))
        assertTrue(ok.primaryValue.contains("42 gtt/min"))
    }

    @Test
    fun `dosage calculator validates non-positive inputs and calculates correct volume`() {
        val invalid = ClinicalCalculators.calculateDosage(0.0, 250.0, 5.0)
        assertTrue(invalid is CalcResult.ValidationError)

        val valid = ClinicalCalculators.calculateDosage(500.0, 250.0, 5.0) as CalcResult.Success
        assertTrue(valid.primaryValue.contains("10.0"))
        assertNotNull(valid.safetyWarning)
    }

    @Test
    fun `pediatric dose caps at max daily ceiling`() {
        val res = ClinicalCalculators.calculatePediatricDose(
            weightKg = 40.0,
            mgPerKgPerDay = 50.0,
            dosesPerDay = 2,
            maxDailyMg = 1500.0
        ) as CalcResult.Success
        assertTrue(res.primaryValue.contains("750.0 mg / dose"))
        assertTrue(res.primaryValue.contains("1500.0 mg/day"))
    }

    @Test
    fun `gcs 8 or below triggers critical airway warning`() {
        val res = ClinicalCalculators.evaluateGcs(2, 2, 4) as CalcResult.Success
        assertEquals("GCS 8 / 15  (E2 V2 M4)", res.primaryValue)
        assertNotNull(res.safetyWarning)
    }

    @Test
    fun `bmi and temperature conversions work accurately`() {
        val bmi = ClinicalCalculators.calculateBmi(70.0, 175.0) as CalcResult.Success
        assertTrue(bmi.primaryValue.contains("22.9"))

        val temp = ClinicalCalculators.convertTemperature(38.0, true) as CalcResult.Success
        assertTrue(temp.primaryValue.contains("100.4 °F"))
    }
}

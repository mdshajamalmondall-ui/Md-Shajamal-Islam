package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nursing_ai_prefs")

data class UserPreferences(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val userName: String = "Nurse Scholar",
    val userRole: UserRole = UserRole.STUDENT,
    val dailyGoalQuestions: Int = 10,
    val completedTodayQuestions: Int = 3,
    val studyStreakDays: Int = 5,
    val clinicalSafetyAccepted: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ROLE = stringPreferencesKey("user_role")
        val DAILY_GOAL = intPreferencesKey("daily_goal_questions")
        val COMPLETED_TODAY = intPreferencesKey("completed_today_questions")
        val STUDY_STREAK = intPreferencesKey("study_streak_days")
        val SAFETY_ACCEPTED = booleanPreferencesKey("clinical_safety_accepted")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            language = AppLanguage.entries.firstOrNull { it.code == prefs[Keys.LANGUAGE] } ?: AppLanguage.ENGLISH,
            themeMode = ThemeMode.entries.firstOrNull { it.key == prefs[Keys.THEME_MODE] } ?: ThemeMode.LIGHT,
            userName = prefs[Keys.USER_NAME] ?: "Nurse Scholar",
            userRole = UserRole.entries.firstOrNull { it.key == prefs[Keys.USER_ROLE] } ?: UserRole.STUDENT,
            dailyGoalQuestions = prefs[Keys.DAILY_GOAL] ?: 10,
            completedTodayQuestions = prefs[Keys.COMPLETED_TODAY] ?: 3,
            studyStreakDays = prefs[Keys.STUDY_STREAK] ?: 5,
            clinicalSafetyAccepted = prefs[Keys.SAFETY_ACCEPTED] ?: true
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.key }
    }

    suspend fun setUserProfile(name: String, role: UserRole) {
        context.dataStore.edit {
            it[Keys.USER_NAME] = name.ifBlank { "Nurse Scholar" }
            it[Keys.USER_ROLE] = role.key
        }
    }

    suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL] = goal.coerceIn(5, 50) }
    }

    suspend fun incrementCompletedQuestions(count: Int = 1) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.COMPLETED_TODAY] ?: 3
            prefs[Keys.COMPLETED_TODAY] = current + count
        }
    }
}

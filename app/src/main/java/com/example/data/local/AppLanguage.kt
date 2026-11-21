package com.example.data.local

enum class AppLanguage(val code: String, val displayLabel: String) {
    ENGLISH("en", "English"),
    BENGALI("bn", "বাংলা")
}

enum class ThemeMode(val key: String, val labelEn: String, val labelBn: String) {
    SYSTEM("system", "System Default", "সিস্টেম ডিফল্ট"),
    LIGHT("light", "Clean Light", "হালকা থিম"),
    DARK("dark", "Clinical Dark", "ডার্ক থিম")
}

enum class UserRole(val key: String, val labelEn: String, val labelBn: String) {
    STUDENT("student", "Nursing Student", "নার্সিং শিক্ষার্থী"),
    REGISTERED_NURSE("rn", "Registered Nurse", "রেজিস্টার্ড নার্স"),
    EDUCATOR("educator", "Nursing Educator", "নার্সিং প্রশিক্ষক"),
    ADMIN("admin", "Content Admin", "কন্টেন্ট অ্যাডমিন")
}

fun bi(language: AppLanguage, en: String, bn: String): String =
    if (language == AppLanguage.BENGALI) bn else en

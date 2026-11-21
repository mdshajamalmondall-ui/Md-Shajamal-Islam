# Contributing to NURSING AI

Thank you for helping improve **NURSING AI**! Because this is a healthcare education application, all code and clinical content contributions must follow strict safety and quality standards.

## Clinical Content Guidelines
1. **Trusted Sources Only:** All nursing notes, drug monographs, procedures, and MCQs must cite verifiable clinical guidelines or standard nursing textbooks (e.g., WHO, CDC, NICE, AHA, ADA, ISMP, BNMC, Potter & Perry, Brunner & Suddarth).
2. **Never Fabricate References:** Include accurate source titles, URLs, and update dates.
3. **High-Alert & Supervision Notices:** Clearly mark High-Alert medications and invasive clinical skills requiring supervised training.
4. **Bilingual Accuracy:** Keep English and Bengali (`বাংলা`) clinical terminology accurate and patient-safe.

## Development Workflow
1. Fork the repository and create a feature branch (`git checkout -b feature/my-update`).
2. Follow Kotlin + Jetpack Compose Material 3 conventions and keep files modular.
3. Add or update unit/Robolectric tests in `app/src/test/`.
4. Verify locally before opening a pull request:
   ```bash
   ./gradlew :app:testDebugUnitTest :app:assembleDebug
   ```

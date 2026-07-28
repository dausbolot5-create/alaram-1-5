# memory_log — alaram

## 2026-07-28

- Project cloned from https://github.com/dausbolot5-create/alaram
- Location: D:\Obsidian\Agent\BaseObsidian\alaram
- Stack: Android (Kotlin/Jetpack Compose) + Web (React/Vite/TypeScript)
- Features: exam scheduler with alarm, Gemini AI integration, calendar, ICS export
- Has both Android app (app/) and web PWA (src/) in one repo

### Bugfixes & Features
- Synced manual entry form in UploadScreen to use `ExamFormDialog` component, matching DashboardScreen behavior.
- Moved Gemini API Key to frontend settings (SettingsScreen). Users can now input their own API key and select AI model (gemini-1.5-flash, pro, etc).
- Added a "Test Koneksi AI" button in SettingsScreen to verify the provided API Key.
- Changed `<input type="time">` to standard text inputs (`pattern="\d{2}:\d{2}"`) in forms to enforce 24-hour HH:mm format and bypass OS locale AM/PM formatting.

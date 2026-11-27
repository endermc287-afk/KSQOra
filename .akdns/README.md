# KSQora Android

Native Android application source built with Kotlin and Jetpack Compose.

## Build APK with GitHub Actions
1. Upload the contents of this ZIP into the root of your `KSQOra` GitHub repository, preserving the folder paths.
2. Open the repository's **Actions** tab.
3. Select **Build KSQora Android APK** and choose **Run workflow** (or push a commit that changes `android/**`).
4. When the run completes, open it and download the `KSQora-debug-apk` artifact.
5. Extract the artifact ZIP to get `app-debug.apk`.

## Current MVP features
- Ten Azerbaijani school subjects.
- Create an assessment with multiple-choice questions.
- Share assessments as a text code through Android's share sheet.
- Students can import the code, answer questions, and see their score.
- Assessments are stored locally on the device.

## Important limitations
This starter version does not yet include user accounts, cloud synchronization, online share URLs, or a central teacher results dashboard. The creator badge is currently a visual label in the app, not verified account ownership. Those features require a secure backend and authentication.

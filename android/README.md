# ANGEL'S LIFE — Android prototype

This is a **debug-only offline prototype** for the three Angel Life forms
(Incidente, Ley HIPAA and Progress Notes). The official logo is packaged
from `../assets/logo-oficial.png` together with `../index.html`.

## Build

Use GitHub Actions → **Android prototype APK**. A successful run uploads
`angels-life-prototype-debug` (containing `app-debug.apk`). The workflow
also checks that the HTML and official PNG are actually inside the APK.
To build locally, install JDK 17, Android SDK platform 35 / build-tools
35.0.0, and Gradle 8.9, then run `gradle :app:assembleDebug` from `android/`.

The native **PDF / Imprimir** button uses Android's Print framework. Choose
**Save as PDF** in the print destination. Only the active form is printed.
Rotation preserves the in-memory form entries and signature canvases.
External network loads and the JavaScript-to-native bridge are disabled.

## Patient safety / deployment gate

**Only use fabricated test data. Do not enter real patient names, identifiers,
medical records or signatures.** This prototype is not suitable for clinical
use: identity/authentication, encryption at rest, access controls, retention,
audit trails, verified clinical form wording and HIPAA/legal review remain
unimplemented. PDF export may create a file outside this application, so
testing should use synthetic information only.

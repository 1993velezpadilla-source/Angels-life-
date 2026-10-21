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

## Incident sheet v0.4

- Mobile-friendly sections, tappable option matrices and quick-jump navigation.
- Medical-plan dropdown with **four ASES-confirmed Vital insurers** (First
  Medical, MMM Multi Health, Plan de Salud Menonita, Triple-S), other recognized
  commercial insurers, internal Doctor Center Arecibo agreement and free entry
  options for private/unlisted plans. This is **not an exhaustive list of every
  insurance product currently sold in Puerto Rico**. Coverage must be checked
  against the patient's current card before use.
- Medical licence field with numeric keypad, separate patient service vs.
  transportation refusal signatures, structured assessment, BP numerator/
  denominator and explicitly visible checkbox values during printing.
- Always keep the full paper form and any additional approved source material
  as the legal/clinical source of truth; this digital prototype does not replace it.

Sources checked on 2026-10-21:
- ASES Vital: https://www.ases.pr.gov/plan-vital
- 2026 OCS group coverage: https://www.ocs.pr.gov/seguros-de-salud/cubiertas/2026/cubiertas-grupos-pequenos
- 2026 OCS individual coverage: https://www.ocs.pr.gov/en-us/seguros-de-salud/cubiertas/2026/cubiertas-individuales

Browser CI now runs `python3 tests/test_form_browser.py` against real Chrome to
exercise dropdowns, checkbox rendering, signatures, printing mirrors and tabs.

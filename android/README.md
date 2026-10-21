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

## V5 TEST installation / signing fix (21 Oct 2026)

Every GitHub-hosted **debug** build uses a temporary Android debug signing
keystore. Android rejects updates when signer certificates differ, even if the
versionCode increases. The 0.3.2 and 0.4.0 APKs had different signing
certificates; trying to install one over the other can leave the older app on
the phone. V5 TEST therefore has its own **distinct package ID**:
`pr.angelslife.forms.preview05` and launcher label **ANGEL LIFE V5 TEST**.
It installs **beside** the older prototype rather than silently failing to
upgrade it. Open the app with the **V5 TEST** label, not the old ANGEL'S LIFE.

This is intentionally a temporary testing solution, not a long-term release
signing solution. Proper upgrades will require one stable protected signing
key/keystore stored as a repository secret or a secure build service, never
committed into this public repository.

### V5 tester checklist

1. Download the ZIP produced by the latest GREEN GitHub Actions run and open its `app-debug.apk`.
2. **Install as a second app:** find the separate launcher icon named `ANGEL LIFE V5 TEST`.
3. The native top bar must say `ANGEL LIFE · V5 TEST`, with a green `Guardar PDF` button.
4. The first screen must show six big navigation cards for sections 01–06.
5. If the launcher or native toolbar does not show `V5 TEST`, the older APK is running — do not interpret that as a visual test failure.

Android prototype signature changes are confirmed from the v0.3.2 and v0.4.0
APK signing certificates (SHA-256 differed). Avoid encouraging repeated
attempts to install newer debug APKs over older debug builds.

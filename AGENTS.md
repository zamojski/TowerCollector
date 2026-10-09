# AGENTS.md — How to be productive in this codebase

This document gives an AI coding agent the minimum, concrete knowledge needed to make safe, high-value changes in TowerCollector.

1) Big picture
- Android app (module: `app`) built with Gradle. Namespace: `info.zamojski.soft.towercollector`.
- Two flavor dimensions: `environment` (develop, production) and `market` (official, fdroid).
- Build variants are combinations: e.g. `developOfficialDebug`, `productionFdroidRelease`.
- Market-specific code and resources live in `app/src/official` and `app/src/fdroid` respectively.

2) Important files & locations (examples)
- `app/build.gradle` — central build logic: flavors, signing config resolution, buildConfig fields and helper functions (`rot135`, `rotHex`, `getLastGitCommitTimestamp`).
- `app/properties/` — contains keystores and `private*.properties` used at build-time to set signing and secret buildConfig fields. The gradle script looks for `private-<flavor>.properties`, then `private.properties`, then `example.properties`.
- `app/google-services.json` (or `google-services-example.json`) — used by the Google services plugin; plugin is enabled only for the `official` flavor (see `afterEvaluate` in `app/build.gradle`).
- `app/src/main/res/raw/changelog.json` — release changelog; contains entries with `VersionCode` values that match `versionCode` in `app/build.gradle`. When bumping version you usually update both.
- `app/proguard-rules.pro` and `app/mapping.txt` — ProGuard/R8 config and produced mapping (mapping may be large; keep it out of changes unless intentionally updating mappings).
- `fastlane/metadata/android/` — store metadata for Play/F-Droid releases (localized descriptions, screenshots).

3) Build & release notes (concrete commands)
- Local debug build (Windows PowerShell):
  .\gradlew.bat :app:assembleDevelopOfficialDebug
- Local release build (requires keystore + properties in `app/properties`):
  .\gradlew.bat :app:assembleProductionOfficialRelease
- Build F‑Droid release:
  .\gradlew.bat :app:assembleProductionFdroidRelease
- Run unit tests: `.\gradlew.bat test` — instrumentation tests (device/emulator): `.\gradlew.bat connectedAndroidTest`.

4) Project-specific conventions & gotchas
- Secrets and signing: signing properties are not in source control. `app/build.gradle` asserts the keystore exists and reads `app/properties/private*.properties`. Do not hardcode secrets; use the existing properties pattern.
- Rot/obfuscation helpers: `rot135` and `rotHex` are used to lightly obfuscate some property values before embedding into BuildConfig. `ext.rotEnabled` defaults to `false` at top of `app/build.gradle`. If you see rotated strings in properties, look for these functions.
- Git timestamp: build generates `BUILD_DATE_TIME` using `git show --no-patch --format=%ct`. The git CLI must be available in CI/build environment or this will fail.
- Google services plugin: disabled for non-official flavors by `afterEvaluate` (it programmatically disables tasks). If changing plugin usage, preserve this behavior or adjust tasks accordingly.
- Version sync: `app/build.gradle` contains `versionCode`/`versionName`. The `changelog.json` in `res/raw` contains entries with `VersionCode` numbers — keep these in sync when releasing.
- MinSdk & Java: minSdkVersion 23, Java source/target compatibility set to 1.8.

5) Common edit patterns you will see
- Adding flavor-specific behavior: add fields to `setBuildConfigFieldsFromProps` and provide corresponding `private-<flavor>.properties` entries.
- Adding native libraries or new dependencies: edit `app/build.gradle` dependencies; prefer using `officialImplementation` for analytics-only libs (see firebase analytics example).
- Feature flags and availability: many runtime toggles come from BuildConfig fields (ACRA, ANALYTICS_AVAILABLE, MARKET_NAME). Update properties and `setBuildConfigFieldsFromProps` when introducing new toggles.

6) External integrations to be aware of
- Firebase Analytics — only available for `official` flavor (see `officialImplementation 'com.google.firebase:firebase-analytics:...'`).
- ACRA crash reporting — configured via buildConfig fields loaded from properties; multiple ACRA modules are used (`acra-http`, `acra-dialog`, `acra-limiter`). See `setBuildConfigFieldsFromProps` for property names.
- Google Play / Fastlane — Play metadata in `fastlane/metadata/android/`. Keep version and release notes in sync.

7) When you make changes — checklist
- Update `app/build.gradle` (versionCode/versionName) if releasing.
- Update `app/src/main/res/raw/changelog.json` entry `VersionCode` and add new Messages if needed.
- Ensure `app/properties/private*.properties` contains required keys for new BuildConfig fields (or provide reasonable defaults in `example.properties`).
- Run a local build for the affected variants and verify no signing or git timestamp failures.

If you need more context about a specific area (build, signing, flavors, ACRA, Firebase, resource conventions), open the relevant file under `app/` and I will extract targeted guidance.


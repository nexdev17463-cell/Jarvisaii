# JARVIS AI — source update notes

## Included changes
- Added a Settings tab with theme selection, voice reply toggle, privacy notes, and app information.
- Bumped app version to 1.1 (versionCode 2).
- Removed the Gemini API key BuildConfig field and stopped making direct Gemini API requests from the APK.
- GitHub Actions now builds a debug APK without injecting an API key.

## Important functionality note
Cloud AI replies are intentionally disabled until a trusted backend is configured. Do not put a Gemini API key in Kotlin, Gradle BuildConfig, or an APK. A backend should hold the key server-side and apply authentication, rate limits, and usage limits.

## Signing note
The GitHub Actions artifact is a debug APK, not a Play Store release. A release keystore can be created locally for free, but its private keystore and passwords must never be committed or uploaded in this source ZIP. Use Android's official signing workflow and keep a secure backup of the keystore. Changing signing keys can prevent in-place updates over an existing install.

## Play Protect
A valid APK signature does not guarantee an app is safe and cannot guarantee that Play Protect will not warn. Do not bypass a warning. Review any warning and test builds only from source you trust.

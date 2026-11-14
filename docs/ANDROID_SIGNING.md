# Android release signing and Obtainium updates

**Current authoritative status — 2026-11-14:** The user explicitly approved a fresh signing key **before any published signed release or installed release update chain**. The old key/certificate identity is superseded for future Player builds. A NEW `player-release.jks` file was generated and verified in the current ChatGPT session and provided privately as downloadable files. It is NOT in the repository. The existing old signing-key files cannot be erased from users' previously saved locations by the AI; they must simply not be used for Player's first release.

## Current permanent release identity

- Application ID: `com.batmancpu.player` (keep unchanged once first release APK is installed).
- Alias: `player-release`.
- Keystore format: JKS, RSA 3072, SHA256withRSA, validity 10,000 days.
- Signing certificate SHA-256: `4D:2F:17:89:ED:27:D3:96:13:35:FE:36:45:64:55:BD:9A:31:0B:0D:CD:F4:1E:8C:CC:92:53:98:33:9E:80:BF`.
- Store password and key password: identical random values in the **new** private `PLAYER_RELEASE_CREDENTIALS.txt`.
- The generated key was validated with `keytool -list -v` using its private password. No signed APK/release has yet been verified with this key.

**Old historical signing fingerprint is superseded** by the one above. Earlier signing handoff history is kept only as provenance; do not use its old fingerprint, key or old credentials. After the FIRST signed release install, DO NOT replace or rotate the new key again, because Android updates require a compatible certificate, an identical applicationId and higher versionCode.

## Private files delivered in the current ChatGPT session

- `player-release.jks` — essential signing key, save a second backup offline or in a secure private vault.
- `PLAYER_RELEASE_KEYSTORE_BASE64.txt` — Base64 representation of that key; NOT encryption, handle as sensitive key material.
- `PLAYER_RELEASE_CREDENTIALS.txt` — generated password and alias; save password in Proton Pass, do not share it in chat.
- `PLAYER_RELEASE_CERTIFICATE_FINGERPRINT.txt` — public certificate fingerprint.

These files are conversation downloads and must NEVER be committed to the repository or pasted into an issue, a chat message or a CI log.

## Enter exactly four GitHub Actions repository secrets

Open https://github.com/Batmancpu/Player/settings/secrets/actions and select **New repository secret** separately for each:

| Name | Secret value |
|---|---|
| `PLAYER_RELEASE_KEYSTORE_BASE64` | All text from the NEW `PLAYER_RELEASE_KEYSTORE_BASE64.txt`, no extra characters |
| `PLAYER_RELEASE_KEYSTORE_PASSWORD` | Password in the NEW `PLAYER_RELEASE_CREDENTIALS.txt` |
| `PLAYER_RELEASE_KEY_ALIAS` | Literal text `player-release` |
| `PLAYER_RELEASE_KEY_PASSWORD` | The SAME password from the NEW credentials file |

The connector cannot edit repository Actions secrets. Only the GitHub account owner should enter the four values via GitHub Settings.

## Produce a GitHub Release APK

1. Once all four secrets are configured, go to GitHub → Actions → Android CI → Run workflow on `main`. Check **Publish verified signed APK to GitHub Releases for Obtainium**.
2. The workflow runs unit tests/build, reads secrets securely, builds the signed APK, checks the exact fingerprint above via `apksigner` and publishes a GitHub Release with APK asset, SHA-256 and version information.
3. Open https://github.com/Batmancpu/Player/releases and download only the signed release APK, not the temporary debug CI artifact.
4. In Obtainium add `https://github.com/Batmancpu/Player`. Later signed releases (same applicationId + certificate, higher versionCode) update in place.

The manual signed publication path has NOT been exercised yet. CI is not the same as phone acceptance.

## Preserve update continuity

- Keep `player-release.jks` and its password in secure, separate backups. Avoid additional key regeneration.
- Do not change `com.batmancpu.player` after the first release install if in-place updates are required.
- Future app display names can change without changing applicationId or signing key.
- If you have a debug APK already installed, you may need one initial uninstall for the first signed release; afterward continue using only signed APK updates.
- No broad storage access or media files are included in this key workflow.

Android reference: https://developer.android.com/studio/publish/app-signing

# Android release signing and Obtainium updates

**Current authoritative status — 2026-11-14:** The user explicitly approved a fresh signing key **before any published signed release or installed release update chain**. The old key/certificate identity is superseded for future Player builds. A NEW `player-release.jks` file was generated and verified in the current ChatGPT session and provided privately as downloadable files. It is NOT in the repository. The existing old signing-key files cannot be erased from users' previously saved locations by the AI; they must simply not be used for Player's first release.

## Current permanent release identity

- Legacy application ID: `com.batmancpu.player` (v0.1.250 legacy release). User explicitly requested new brrPlayer ID `com.mangoloads.player`; it is a separate app and does not update or inherit data from the legacy package. Keep the permanent certificate and use higher run-number versionCodes.
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

## Required GitHub Actions repository secrets

Open https://github.com/Batmancpu/Player/settings/secrets/actions and select **New repository secret** separately for each:

| Name | Secret value |
|---|---|
| `PLAYER_RELEASE_KEYSTORE_BASE64` | All text from the NEW `PLAYER_RELEASE_KEYSTORE_BASE64.txt`, no extra characters |
| `PLAYER_RELEASE_KEYSTORE_PASSWORD` | Password in the NEW `PLAYER_RELEASE_CREDENTIALS.txt` |
| `PLAYER_RELEASE_KEY_ALIAS` | No longer required: the public alias `player-release` is fixed in CI; a previously saved incorrect value is ignored |
| `PLAYER_RELEASE_KEY_PASSWORD` | Optional: same password as the keystore password; CI falls back to the store password if omitted |

Only `PLAYER_RELEASE_KEYSTORE_BASE64` and `PLAYER_RELEASE_KEYSTORE_PASSWORD` are required. The public alias `player-release` is configured directly in CI. The fourth, `PLAYER_RELEASE_KEY_PASSWORD`, is optional for the current keystore because its private-key password is identical to the store password; CI reuses `PLAYER_RELEASE_KEYSTORE_PASSWORD` if the separate key-password secret is empty. The connector cannot edit GitHub secrets.

## Produce a GitHub Release APK

1. Once all four secrets are configured, go to GitHub → Actions → Android CI → Run workflow on `main`. Check **Publish verified signed APK to GitHub Releases for Obtainium**.
2. The workflow runs unit tests/build, reads secrets securely, builds the signed APK, checks the exact fingerprint above via `apksigner` and publishes a GitHub Release with APK asset, SHA-256 and version information.
3. Open https://github.com/Batmancpu/Player/releases and download only the signed release APK, not the temporary debug CI artifact.
4. In Obtainium add `https://github.com/Batmancpu/Player`. The new brrPlayer package installs separately. Later signed brrPlayer releases with the same `com.mangoloads.player`, certificate and higher versionCode update in place.

Signed publication passed for v0.1.51 and v0.1.53 via the controlled release(test): trigger. The user confirmed their in-place Obtainium update. No further signing setup is required; Phase 1 functionality acceptance is separate.

## Preserve update continuity

- Keep `player-release.jks` and its password in secure, separate backups. Avoid additional key regeneration.
- The user authorized migration to `com.mangoloads.player`. Android treats it as a distinct app; do not imply it can update `com.batmancpu.player` or inherit its app-private data/SAF grant. Continue keeping `com.mangoloads.player` stable after its first signed installation.
- Future app display names can change without changing applicationId or signing key.
- If you have a debug APK already installed, you may need one initial uninstall for the first signed release; afterward continue using only signed APK updates.
- No broad storage access or media files are included in this key workflow.

Android reference: https://developer.android.com/studio/publish/app-signing


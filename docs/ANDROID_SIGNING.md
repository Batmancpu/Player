# Android release signing and update continuity

**Current status (2026-11-14):** a permanent release keystore has been generated and verified. It was delivered to the user as private downloadable files in the current ChatGPT conversation. It is not stored in this public repository. The user must save the JKS securely, store its password in Proton Pass or another password manager, and add the four GitHub Actions repository secrets. No signed Release APK has been produced yet.

## Why this exists

Android APK updates require the same application ID, a compatible signing identity (normally the same certificate), and a higher `versionCode`. The one permanent signing key can be reused for the app's entire life.

The signing key is **not tied to the application ID**. Renaming the app or changing its package/application ID later does not require another signing key. However, changing the application ID makes Android treat the result as a different app: it will install as a separate app and cannot update the old package in place. The user wants the final brand/package decision deferred while core functionality is built.

Official Android guide: https://developer.android.com/studio/publish/app-signing

## Generated key identity

- Keystore file: `player-release.jks`
- Key alias: `player-release`
- Store password and key password: same generated random password; see the private credentials file delivered with the keystore.
- Algorithm: RSA 2048, SHA256withRSA.
- Validity: 10,000 days.
- Public SHA-256 certificate fingerprint: `62:A0:97:B7:BE:7D:22:A9:88:3D:9E:09:01:5C:04:17:72:72:16:D7:3E:25:06:ED:F4:70:B2:4A:61:CD:AD:C0`.

The fingerprint is public and safe to record. **Never commit or publish the JKS, the base64 value, or the password.**

## Files delivered to the user

1. `player-release.jks`: original private keystore. Keep a primary copy and at least one separate secure backup.
2. `PLAYER_RELEASE_KEYSTORE_BASE64.txt`: one-line base64 encoding used as the GitHub secret. Base64 is not encryption; treat this file as the private key.
3. `PLAYER_RELEASE_CREDENTIALS.txt`: generated store/key password, alias and public fingerprint. Save the password in Proton Pass, then delete unnecessary plaintext copies.
4. `PLAYER_RELEASE_CERTIFICATE_FINGERPRINT.txt`: public certificate details; safe to retain separately.

The files were generated and the keystore was reopened with `keytool -list` to verify the alias/password. No key material has been added to GitHub source, repository history, this documentation or CI logs.

## Configure the four GitHub repository secrets

Open: https://github.com/Batmancpu/Player/settings/secrets/actions

Create these exact names as **repository secrets**:

| Secret name | Value |
|---|---|
| `PLAYER_RELEASE_KEYSTORE_BASE64` | Entire one-line contents of `PLAYER_RELEASE_KEYSTORE_BASE64.txt` |
| `PLAYER_RELEASE_KEYSTORE_PASSWORD` | Password in `PLAYER_RELEASE_CREDENTIALS.txt` |
| `PLAYER_RELEASE_KEY_ALIAS` | `player-release` |
| `PLAYER_RELEASE_KEY_PASSWORD` | Same password as the keystore password |

Do not put the password or base64 string in chat, issues, source files, ordinary repository variables or workflow inputs. The GitHub connector available to AI agents does not expose a repository-secret write action, so the repository owner must enter these values through Settings.

## Verify the first signed build

After all four secrets are saved, trigger **Android CI** from its Actions page, or let the next push to `main` run it. The workflow will decode the keystore into the runner's temporary directory, validate the password and alias without printing secrets, run tests, assemble the signed Release APK, verify it with `apksigner`, report the public certificate fingerprint and APK SHA-256, and upload a separately named `Player-release-v...` artifact.

Compare the CI fingerprint with the fingerprint above. Only the signed Release artifact is the stable update channel. Debug artifacts are marked CI-only; do not use them as routine installs.

Gradle derives versionCode from the monotonically increasing GitHub Actions run number. Never reset or reuse a released version code. The workflow skips Release signing when all four secrets are absent and fails if only some are configured.

## Backup and update rules

- Never regenerate, rotate, overwrite or replace this key just to rename or redesign the app.
- Keep the original JKS and at least one separate backup. Keep the password in Proton Pass or another password manager. Do not keep the only key copy and its only password copy in the same place.
- If the key is lost, an app installed under the old signing certificate cannot be updated with a newly generated key. Do not create a replacement casually.
- Current source application ID is `com.batmancpu.player`, but it is provisional. User wants the branding/package makeover deferred until core functionality works.
- If the application ID changes later, reuse this same signing key. Android treats the new application ID as a different app; it cannot update the old package in place.
- A one-time uninstall may be necessary when moving from an already-installed debug APK signed with another certificate to the first stable-key Release APK. After that migration, use only signed Release artifacts for in-place updates.

## Publishing updates to GitHub Releases / Obtainium

The workflow supports an explicit manual Release publication gate without publishing an APK for every docs/code commit.

1. Configure the four encrypted GitHub Actions secrets listed above, using the previously delivered **permanent** key. Do not share secrets in chat.
2. Open GitHub -> Actions -> Android CI -> Run workflow on **main**. Enable **Publish verified signed APK to GitHub Releases for Obtainium**.
3. The workflow runs JVM tests and builds, verifies the signed APK certificate against the permanent public fingerprint, then publishes a GitHub Release APK with SHA-256. Missing/wrong secrets or a fingerprint mismatch fail publication.
4. Add https://github.com/Batmancpu/Player to Obtainium as a GitHub source. Each new release retains the certificate/package and uses a higher versionCode.
5. The user must test each phase's APK on CPH2531 before its acceptance is recorded.

As of 2026-11-14, no signed APK was created in CI run 36; repository secrets were absent. Do not claim a Release exists until workflow publication succeeds.

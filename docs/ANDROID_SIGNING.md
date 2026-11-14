# Android release signing and update continuity

**Status (2026-11-14):** Gradle and CI signing support is committed. The user's private keystore has not yet been generated, and GitHub Actions secrets have not yet been configured. No signed release APK is available until those two user-side steps are completed.

## Why this is required

Android accepts an in-place APK update only when the new APK is signed by the same signing certificate as the installed APK, and the new build must have a higher versionCode. A different signing certificate requires uninstalling the old app before installing the new one. The release pipeline therefore uses one permanent keystore and a monotonically increasing CI run number.

Official Android guide: https://developer.android.com/studio/publish/app-signing

**Do not install artifacts named Player-debug-CI-ONLY-... for routine testing.** They use the CI debug certificate and are not the update channel. Install the Player-release-v... artifact once signing secrets are configured; subsequent main-branch builds will use the same release key.

## 1. Generate the keystore on the Android phone

Install Termux from one official source (F-Droid or the Termux GitHub release; do not mix Termux app/plugins from different signing sources). Termux maintainers' installation guidance: https://github.com/termux/termux-app.

In Termux, run:

~~~sh
pkg update -y
pkg install openjdk-21 coreutils -y

mkdir -p ~/player-signing
cd ~/player-signing
umask 077

keytool -genkeypair -v \
  -keystore player-release.jks \
  -storetype JKS \
  -alias player-release \
  -keyalg RSA \
  -keysize 2048 \
  -sigalg SHA256withRSA \
  -validity 10000 \
  -dname "CN=Player Release, OU=Android, O=Player Project, L=Gurugram, ST=Haryana, C=IN"
~~~

When prompted, enter a strong, unique keystore password and save it in a password manager. When keytool asks for the key password, press Enter to reuse the keystore password. The two credentials are stored under two separate GitHub secret names for the Gradle configuration.

The key is generated in Termux's private home directory, not in shared Downloads and not in the Git repository. Do not upload the raw JKS file to GitHub, a chat, a public cloud folder or an issue.

## 2. Verify and back up the key

Run:

~~~sh
cd ~/player-signing
keytool -list -v -keystore player-release.jks -alias player-release
~~~

Check that the alias is player-release, the keystore opens with your saved password, and the certificate validity is about 27 years. Record its SHA-256 certificate fingerprint. Keep at least one additional offline backup of player-release.jks in a secure place separate from the phone. Keep the password separate from the file. Do not delete the original Termux copy.

## 3. Prepare the base64 secret value

In Termux:

~~~sh
cd ~/player-signing
base64 -w 0 player-release.jks
~~~

Copy the complete output. It is the encoded private keystore, so handle it exactly like the keystore itself. Do not paste it into this conversation.

## 4. Add four GitHub Actions repository secrets

Open https://github.com/Batmancpu/Player/settings/secrets/actions, choose **New repository secret**, and add these exact names:

| Secret name | Value |
|---|---|
| PLAYER_RELEASE_KEYSTORE_BASE64 | Complete base64 output from step 3 |
| PLAYER_RELEASE_KEYSTORE_PASSWORD | Keystore password |
| PLAYER_RELEASE_KEY_ALIAS | player-release |
| PLAYER_RELEASE_KEY_PASSWORD | Same password (because keytool used the keystore password for the key) |

GitHub stores Actions secrets encrypted and only exposes them to workflows that explicitly request them. Do not add these values to repository files, ordinary Actions variables, workflow_dispatch inputs or logs. Repository secrets are limited to 48 KB; this JKS base64 value should be well below that limit.

## 5. Verify the first stable-key APK

After all four secrets are saved, a push to main or a manual run of **Android CI** will:

1. Decode the keystore into the runner's temporary directory with restrictive file permissions.
2. Validate the keystore password and alias without printing secrets.
3. Run unit tests and assemble the CI-only debug APK.
4. Assemble the signed Release APK with the saved release key.
5. Verify the APK signature and display only the public certificate SHA-256 fingerprint and APK SHA-256.
6. Upload an artifact named Player-release-v<run-number>-<commit>.

Download and install that signed release APK. Compare the certificate fingerprint with the one recorded from keytool. On the next main-branch build, install the new signed release APK directly over the previous one. The app should retain its data and update in place, provided the application ID is unchanged and the version code is higher.

## Important identity and recovery rules

- Current Android application ID: com.batmancpu.player. It is provisional until the first signed install. Decide whether to keep it before installing the first signed release.
- After the first signed install, never change applicationId if the goal is in-place updates. Changing the visible app label is safe; changing the package/application ID creates a different Android app.
- Never lose or casually rotate the keystore. Without the original signing key, future APKs cannot update the existing self-managed installation. Keep a secure backup and password record.
- If a different-key debug/test build is already installed, one uninstall may be needed when migrating to this release key. After the first stable-key release install, never use debug artifacts as the update channel.
- CI run number supplies the increasing versionCode; never manually reset it or reuse a previously released number.
- If the release workflow says signing secrets are missing, it will skip the release artifact rather than produce a misleading installable build. If only some secrets exist or the password/alias is wrong, CI fails.

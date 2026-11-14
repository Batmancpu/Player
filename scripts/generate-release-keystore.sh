#!/usr/bin/env bash
set -euo pipefail

# Run on the user's own Android device in Termux. Never run this in CI or commit its output.
umask 077

readonly SIGNING_DIR="${HOME}/player-signing"
readonly KEYSTORE_PATH="${SIGNING_DIR}/player-release.jks"
readonly BASE64_PATH="${SIGNING_DIR}/PLAYER_RELEASE_KEYSTORE_BASE64.txt"
readonly KEY_ALIAS="player-release"

for required_command in keytool base64 chmod; do
  if ! command -v "${required_command}" >/dev/null 2>&1; then
    printf 'Missing command: %s\\nInstall the required Termux packages documented in docs/ANDROID_SIGNING.md.\\n' "${required_command}" >&2
    exit 1
  fi
done

mkdir -p "${SIGNING_DIR}"
chmod 700 "${SIGNING_DIR}"

if [[ ! -f "${KEYSTORE_PATH}" ]]; then
  printf '\\nCreating the permanent Player release keystore on this device.\\n'
  printf 'Choose a strong password and save it in your password manager.\\n'
  printf 'When asked for the key password, press Enter to reuse the keystore password.\\n\\n'

  keytool -genkeypair -v \\
    -keystore "${KEYSTORE_PATH}" \\
    -storetype JKS \\
    -alias "${KEY_ALIAS}" \\
    -keyalg RSA \\
    -keysize 2048 \\
    -sigalg SHA256withRSA \\
    -validity 10000 \\
    -dname "CN=Player Release, OU=Android, O=Player Project, C=IN"
else
  printf 'Existing keystore found. It will be reused; this script will not rotate or overwrite it.\\n'
fi

chmod 600 "${KEYSTORE_PATH}"

printf '\\nVerify the existing key and record its public SHA-256 certificate fingerprint.\\n'
keytool -list -v -keystore "${KEYSTORE_PATH}" -alias "${KEY_ALIAS}"

printf '\\nPreparing the base64 value for the GitHub Actions secret...\\n'
base64 -w 0 "${KEYSTORE_PATH}" > "${BASE64_PATH}"
chmod 600 "${BASE64_PATH}"

printf '\\nSUCCESS\\n'
printf 'Private keystore: %s\\n' "${KEYSTORE_PATH}"
printf 'Base64 secret file: %s\\n' "${BASE64_PATH}"
printf 'Key alias: %s\\n' "${KEY_ALIAS}"
printf '\\nThe base64 file contains private signing material. Do not paste it into chat, commit it, or share it publicly.\\n'
printf 'Keep at least one separate secure backup of the JKS file and keep the password in your password manager.\\n'

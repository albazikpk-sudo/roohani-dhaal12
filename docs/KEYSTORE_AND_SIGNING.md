# Keystore and signing
Create once (keep the file and passwords safe — **losing it means you cannot update the app** unless you use Play App Signing key reset):
```
keytool -genkeypair -v -keystore roohani-release.keystore -alias roohani -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 roohani-release.keystore > keystore_base64.txt      # macOS: base64 -i roohani-release.keystore
```
GitHub Secrets: `KEYSTORE_BASE64` (contents of keystore_base64.txt), `KEYSTORE_PASSWORD`, `KEY_ALIAS` (e.g. roohani), `KEY_PASSWORD`.
Never commit the keystore (`.gitignore` blocks *.keystore/*.jks). Back it up in 2 separate secure places.
- Debug and release signing are separate: debug uses the default debug key and package suffix `.debug`.
- The workflow verifies the APK (`apksigner verify`) and the AAB (`jarsigner -verify`) and prints the certificate fingerprint.
- Recommended: enrol in **Play App Signing** when first uploading the AAB; your keystore then acts as the *upload key*.
- Every upload to Play needs a higher versionCode; the workflow uses the GitHub run number automatically.

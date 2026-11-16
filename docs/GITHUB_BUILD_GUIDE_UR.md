# GitHub پر APK اور AAB بنانے کی مکمل ہدایات

## مرحلہ 1: Repository بنائیں اور فائلیں اپلوڈ کریں
1. github.com پر لاگ اِن ہوں → **New repository** → نام `roohani-dhaal` → **Private** رکھیں (بہتر ہے) → Create۔
2. ZIP کو اپنے کمپیوٹر پر Extract کریں۔ اندر کا فولڈر `roohani-dhaal` کھولیں؛ اس کے **اندر کی چیزیں** (`android`, `www`, `.github`, `docs`, `tools`, `tests`, `data_src`, `store-assets`, `README.md`, `PRIVACY_POLICY.md`, `.gitignore`) repository کی **جڑ (root)** میں چاہییں۔ ZIP کے اندر ZIP یا اضافی فولڈر نہ بنے۔
3. **طریقہ الف (ویب سے):** repository میں *Add file → Upload files* پر سب فائلیں اور فولڈر ڈراپ کریں → Commit۔
   - اگر `.github` فولڈر چھپا ہونے کی وجہ سے نظر نہ آئے: کمپیوٹر پر hidden files آن کریں۔ ورنہ *Add file → Create new file* میں نام `.github/workflows/android-build.yml` لکھ کر فائل کا مواد پیسٹ کریں، اور اسی طرح `create-keystore.yml`۔
4. **طریقہ ب (git سے، زیادہ قابلِ اعتماد):**
```
cd roohani-dhaal
git init -b main
git add -A
git commit -m "Initial commit"
git remote add origin https://github.com/<آپ کا یوزر نام>/roohani-dhaal.git
git push -u origin main
```
5. تصدیق: repository کے root میں `android` اور `.github/workflows` نظر آنے چاہییں، اور Actions ٹیب میں دو workflows: **Android Build (APK + AAB)** اور **Create Keystore (run ONCE)**۔

## مرحلہ 2: Keystore بنائیں (صرف ایک بار)
1. **Actions → Create Keystore (run ONCE) → Run workflow**۔
2. ختم ہونے پر run کھولیں → نیچے **Artifacts** سے `KEEP-PRIVATE-keystore-and-secrets` ڈاؤن لوڈ کریں (ZIP کھولیں)۔
3. `roohani-release.keystore` کو دو محفوظ جگہوں پر بیک اپ کریں۔ یہ فائل کھو گئی تو اسی ایپ کی اپڈیٹس میں مسئلہ آئے گا۔
4. **Settings → Secrets and variables → Actions → New repository secret** میں `github-secrets.txt` کی چار قدریں بنائیں (نام بالکل یہی):
   `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
5. اب Artifact اور وہ workflow run حذف کر دیں، اور `github-secrets.txt` کسی کو نہ بھیجیں۔

## مرحلہ 3: AdMob IDs (پبلش سے پہلے)
AdMob کنسول میں ایپ، ایک Banner اور ایک Interstitial یونٹ بنائیں، پھر تین مزید Secrets:
`ADMOB_APP_ID` (…~…)، `ADMOB_BANNER_ID` (…/…)، `ADMOB_INTERSTITIAL_ID` (…/…)۔
یہ نہ ہوں تو Release میں Google کے **ٹیسٹ** اشتہارات آئیں گے، ایسا بلڈ Play Store پر نہ ڈالیں۔

## مرحلہ 4: بلڈ چلائیں
**Actions → Android Build (APK + AAB) → Run workflow → Run**۔ تقریباً 10 تا 20 منٹ لگتے ہیں۔
- Keystore کے بغیر فوری ٹیسٹ کے لیے: Run workflow میں `test_key` کا خانہ ✔ کریں۔ فائلوں کے نام `-TESTKEY` پر ختم ہوں گے (صرف ٹیسٹ، Play Store کے لیے نہیں)۔

## مرحلہ 5: فائلیں ڈاؤن لوڈ کریں
مکمل run کھولیں → نیچے **Artifacts**:
| Artifact | استعمال |
|---|---|
| `roohani-dhaal-debug-apk` | فون میں انسٹال کر کے ٹیسٹ (Google ٹیسٹ اشتہارات) |
| `roohani-dhaal-release-apk` | دستخط شدہ APK، انسٹال/تقسیم کے لیے |
| `roohani-dhaal-release-aab` | **Google Play Console پر اپلوڈ کے لیے** |
ہر Artifact ZIP میں ہوتا ہے؛ کھول کر `.apk` / `.aab` نکالیں۔

## اگر بلڈ ناکام ہو
Run میں سرخ ✗ والا step کھولیں اور آخری 30–40 لائنیں کاپی کر کے مجھے بھیجیں؛ نیچے `build-reports` Artifact بھی مل سکتا ہے۔ ایپ کا Java کوڈ اور Gradle ورژنز ابھی کسی حقیقی بلڈ میں نہیں چلے، اس لیے پہلی بار چھوٹی درستی کی ضرورت ممکن ہے۔

## اگلی اپڈیٹس
`android/app/build.gradle` میں versionName بدلیں؛ versionCode ہر بلڈ پر خود بڑھتا ہے (GitHub run number)۔ وہی Keystore ہمیشہ استعمال ہو۔

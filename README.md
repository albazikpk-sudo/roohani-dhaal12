# روحانی ڈھال — Roohani Dhaal
مؤلف مفتی محمد سہیل البازی · Package: `com.albazi.roohanidhaal`

مکمل آف لائن Android ایپ: 172 دعائیں (عربی متن، اردو ترجمہ، حوالہ)، ہر دعا الگ اسکرین پر، آف لائن سرچ، پسندیدہ، روزانہ چیک لسٹ، فونٹ سائز، ڈارک موڈ، کاپی/شیئر، AdMob (اشتہارات اسکرین کے نچلے حصے میں الگ پٹی میں)۔

## ساخت
```
www/                 ویب ایپ (HTML/CSS/JS) + data/duas.json, duas.js (172 دعائیں، بلا تبدیلی)
android/             Android Studio پروجیکٹ (Java + WebView، Capacitor/npm کی ضرورت نہیں)
.github/workflows/   android-build.yml  → Debug APK، Signed Release APK، Signed Release AAB
data_src/            مؤلف کی اصل متن فائل (duas_source.txt)
tools/               parse_duas.py, build_data.py, make_icons.py
tests/               ui_tests.html (Chrome headless)، data_fidelity_test.py + اصل نتائج
docs/                research_register.csv، ADMOB.md، KEYSTORE_AND_SIGNING.md، PLAY_STORE.md، TESTING_REPORT.md
store-assets/        Play Store icon 512×512، splash آرٹ
PRIVACY_POLICY.md
```
**ٹیکنالوجی کا انتخاب:** Capacitor کے بجائے سادہ Java + WebView استعمال ہوا تاکہ `npm`/`npx cap add android` جیسا کوئی جنریشن مرحلہ نہ ہو اور GitHub پر اپلوڈ کے بعد بلڈ میں کم سے کم چیزیں ٹوٹ سکیں۔ ویب فائلیں Gradle خود `assets/www` میں کاپی کرتا ہے (`syncWww` ٹاسک)۔

## GitHub پر بلڈ (مرحلہ وار) — تفصیلی رہنما: `docs/GITHUB_BUILD_GUIDE_UR.md`
1. اس فولڈر کے مواد کو نئی GitHub repository کی جڑ (root) میں اپلوڈ کریں (`android/`, `www/`, `.github/` وغیرہ براہِ راست root میں ہوں)۔
2. Settings → Secrets and variables → Actions میں یہ Secrets بنائیں:
   `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
   (اختیاری، صرف اصل اشتہارات کے لیے: `ADMOB_APP_ID`, `ADMOB_BANNER_ID`, `ADMOB_INTERSTITIAL_ID`)
3. Actions → «Android Build (APK + AAB)» → Run workflow۔
4. مکمل ہونے پر Artifacts سے `roohani-dhaal-debug-apk`، `roohani-dhaal-release-apk`، `roohani-dhaal-release-aab` ڈاؤن لوڈ کریں۔
Keystore نہ ہو تو صرف Debug APK بنے گا اور واضح انتباہ آئے گا۔ تفصیل: `docs/KEYSTORE_AND_SIGNING.md`۔

## مقامی بلڈ
Android Studio میں `android/` فولڈر کھولیں (Gradle wrapper پہلی بار Studio خود بنا دے گا، یا `gradle wrapper --gradle-version 8.13`)۔ Debug بلڈ ہمیشہ Google کے ٹیسٹ اشتہارات استعمال کرتا ہے۔

## اہم نوٹ (فونٹس)
عربی/اردو کے لیے سسٹم فونٹ فہرست دی گئی ہے (Amiri، Noto Naskh، Noto Nastaliq Urdu وغیرہ)۔ آف لائن یکساں شکل کے لیے `.woff2` فونٹ فائلیں `www/fonts/` میں رکھ کر `css/app.css` میں `@font-face` شامل کریں (لائسنس: Amiri اور Noto دونوں OFL ہیں)۔ بلڈ کے دوران انٹرنیٹ نہ ہونے کی وجہ سے میں یہ فونٹ ڈاؤن لوڈ نہیں کر سکا۔

## اس ورژن کی حدود (ایمانداری سے)
- APK/AAB اس ماحول میں **نہیں بنے** (Android SDK اور Google Maven تک رسائی نہیں)۔ GitHub Actions workflow لکھا گیا مگر **ابھی چلا کر جانچا نہیں گیا**۔ تفصیل `docs/TESTING_REPORT.md` میں۔
- دعاؤں کا متن/ترجمہ/حوالے مؤلف کے دیے ہوئے ہیں، بلا تبدیلی شامل ہیں؛ Claude نے ان کی حدیثی تصدیق نہیں کی۔

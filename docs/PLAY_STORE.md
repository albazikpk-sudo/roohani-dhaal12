# Google Play upload checklist
1. Play Console → Create app (name: روحانی ڈھال / Roohani Dhaal; Free; App).
2. Production/Internal testing → upload `roohani-dhaal-release.aab`. Accept Play App Signing.
3. Target API: the project targets **API 36**. Google's published requirement is that new apps/updates target Android 16 (API 36) from 31 Aug 2026 — re-check https://support.google.com/googleplay/android-developer/answer/11926878 at upload time.
4. Store listing: icon `store-assets/play_store_icon_512.png`; add feature graphic 1024×500 and ≥2 phone screenshots (take them from the installed app).
5. App content: Privacy policy URL (host `PRIVACY_POLICY.md` on a public page, fill in your email) · Ads: **Yes, contains ads** · Data safety: declare data collected by AdMob SDK (device/advertising ID, approximate location from IP, diagnostics, app interactions — used for advertising/analytics) per Google's AdMob guidance; no data collected by the app itself · Content rating questionnaire (religious reference app) · App access: no login needed · Target audience: 13+ / general (not children).
6. Permissions in the app: INTERNET, ACCESS_NETWORK_STATE only.

## 2026–2027 Play policy check (Play Console "Policy announcement: October 29, 2026")
Source: https://support.google.com/googleplay/android-developer/answer/18100766
- **Location (27 Jan 2027), READ_CALL_LOG, Manage Contacts, Local Network:** not applicable — this app requests none of these permissions.
- **February 2027 app-quality (memory/code) thresholds:** the app is a light WebView app; still check Android vitals after launch.
- **Trader status declaration (Nov 2026):** complete it in Play Console → required before publishing.
- **Target API:** API 36 meets the Aug 2026 requirement. The page lists Android 17 (API 37) only for specific permission declarations and gives no new target deadline; check Play Console's target-API banner before each update (a later bump to 37 is likely).
- Not covered by that announcement: Data safety and AdMob consent rules — follow the checklist above.

## Getting the first signed AAB without a computer
1. Actions → **Create Keystore (run ONCE)** → download artifact `KEEP-PRIVATE-keystore-and-secrets`, copy the 4 values from `github-secrets.txt` into repository Secrets, back up the keystore file, then delete the artifact and the run.
2. Actions → **Android Build (APK + AAB)** → Run workflow.
For quick install-only testing without a keystore, run the build with `test_key = true` (artifacts end in `-TESTKEY`; never upload those to Play).

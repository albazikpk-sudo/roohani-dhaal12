# AdMob configuration
- **Debug builds** always use Google's official TEST IDs (defined in `android/app/build.gradle`). Safe to click.
- **Release builds** read production IDs only from environment/Gradle properties — nothing is hardcoded:
  `ADMOB_APP_ID` (ca-app-pub-…~…), `ADMOB_BANNER_ID` (ca-app-pub-…/…), `ADMOB_INTERSTITIAL_ID` (ca-app-pub-…/…)
  - GitHub: add them as repository Secrets with those exact names.
  - Local: `gradle assembleRelease -PADMOB_APP_ID=… -PADMOB_BANNER_ID=… -PADMOB_INTERSTITIAL_ID=…`
  - If any is missing, the release build **falls back to TEST ads and prints a warning** — never publish that build.
- Create the app + one banner unit + one interstitial unit in the AdMob console (apps.admob.com) yourself; no IDs were invented.
- Ad behaviour: banner is a native strip below the WebView (cannot overlap Arabic text or Copy/Share). The interstitial is requested only when returning to a list/home screen after ≥8 dua views and at most once per 4 minutes; never on a dua screen. If ads fail or are blocked the strip stays hidden and the app works normally.
- Consent: Google UMP form is shown where required. In AdMob console → Privacy & messaging, create and publish a GDPR message for the app (otherwise EEA users may get no ads).
- Add `app-ads.txt` to your developer website if you list one in Play Console.

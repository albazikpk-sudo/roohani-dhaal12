# Testing Report (only what was actually run, 16 Nov 2026)

## Executed and passed
| Test | How | Result |
|---|---|---|
| Parse author's text → 172 entries, none missing Arabic/Urdu/reference | `tools/parse_duas.py` | 172/172 |
| Data is verbatim copy of author's text (title, Arabic, Urdu, reference) | `tests/data_fidelity_test.py` | 0 failures (`tests/data_fidelity_output.txt`) |
| Web app UI + logic in headless Chrome (file://) | `tests/ui_tests.html` | **50 pass / 0 fail** (`tests/ui_test_output.txt`) |

UI test coverage: all 172 dua screens render Arabic/Urdu/reference identical to data; RTL attributes; Copy button copies Arabic+Urdu+reference and shows «دعا کامیابی سے کاپی ہوگئی۔»; next/prev; favourites and read-marks saved and **persist after reload**; daily checklist; font-size and dark-mode settings; every category list; offline search (diacritics-insensitive, Arabic/Urdu/reference/digits, «نظر بد» → evil-eye duas); app HTML references no http(s) resources.
Also: workflow YAML parses; `app.js` syntax check; logo/icon rendered and visually inspected (Urdu text shaped correctly).

## NOT done — could not be run in this environment
- **Debug APK, Signed Release APK, Signed Release AAB were NOT built.** The sandbox has no Android SDK and cannot reach Google Maven (Gradle failed resolving the Android Gradle Plugin). No APK/AAB exists, so no download links are provided.
- GitHub Actions workflow was **not executed**; first run may need small fixes (dependency versions: AGP 8.13.0, play-services-ads 24.5.0, UMP 3.2.0, androidx.webkit 1.14.0 were chosen but not resolved here).
- Gradle wrapper jar not generated (needs network); the workflow installs Gradle 8.13 itself.
- Java sources (MainActivity) were not compiled. Native Share intent, native Copy bridge, AdMob test ads, consent form, back-button handling, WebView storage persistence on a real device: **untested**.
- Release signature verification: written into the workflow, not run.
- Real-device rendering of Arabic/Urdu fonts: untested (system fonts differ; see README for bundling fonts).


package com.albazi.roohanidhaal;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.webkit.WebViewAssetLoader;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Offline Islamic Dua Application.
 *
 * Features:
 * - WebView based offline application.
 * - Native adaptive Banner Ad at the bottom.
 * - No Interstitial Ads.
 * - UMP consent management.
 * - Ads never cover dua text or buttons.
 */
public class MainActivity extends AppCompatActivity {

    private static final String START_URL =
            "https://appassets.androidplatform.net/assets/www/index.html";

    private WebView webView;
    private FrameLayout adContainer;
    private AdView adView;

    private final AtomicBoolean adsStarted =
            new AtomicBoolean(false);

    @SuppressLint({
            "SetJavaScriptEnabled",
            "AddJavascriptInterface"
    })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.color.green);

        // WebView
        webView = new WebView(this);

        root.addView(
                webView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );

        // Native Banner Ad container
        adContainer = new FrameLayout(this);
        adContainer.setBackgroundColor(0xFFFFFFFF);
        adContainer.setVisibility(View.GONE);

        root.addView(
                adContainer,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(root);

        // System window insets
        ViewCompat.setOnApplyWindowInsetsListener(
                root,
                (v, windowInsets) -> {

                    Insets bars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );

                    Insets ime = windowInsets.getInsets(
                            WindowInsetsCompat.Type.ime()
                    );

                    v.setPadding(
                            bars.left,
                            bars.top,
                            bars.right,
                            Math.max(bars.bottom, ime.bottom)
                    );

                    return WindowInsetsCompat.CONSUMED;
                }
        );

        setupWebView();

        // Android back button
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        webView.evaluateJavascript(
                                "(function(){return window.__rdBack ? window.__rdBack() : false;})()",
                                value -> {

                                    if (!"true".equals(value)) {
                                        setEnabled(false);
                                        getOnBackPressedDispatcher()
                                                .onBackPressed();
                                    }
                                }
                        );
                    }
                }
        );

        webView.loadUrl(START_URL);

        // Consent first, then Banner Ads
        startConsentThenAds();
    }

    // ==================================================
    // WEBVIEW CONFIGURATION
    // ==================================================

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {

        final WebViewAssetLoader loader =
                new WebViewAssetLoader.Builder()
                        .addPathHandler(
                                "/assets/",
                                new WebViewAssetLoader.AssetsPathHandler(this)
                        )
                        .build();

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        settings.setMixedContentMode(
                WebSettings.MIXED_CONTENT_NEVER_ALLOW
        );

        settings.setTextZoom(100);

        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setBackgroundColor(0xFFF7F5EE);

        webView.addJavascriptInterface(
                new Bridge(),
                "Android"
        );

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(
                    WebView view,
                    WebResourceRequest request
            ) {
                return loader.shouldInterceptRequest(
                        request.getUrl()
                );
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                return !"appassets.androidplatform.net".equals(
                        request.getUrl().getHost()
                );
            }
        });
    }

    // ==================================================
    // JAVASCRIPT BRIDGE
    // No Interstitial Ad method
    // ==================================================

    public class Bridge {

        @JavascriptInterface
        public void copyText(final String text) {

            runOnUiThread(() -> {

                ClipboardManager cm =
                        (ClipboardManager) getSystemService(
                                Context.CLIPBOARD_SERVICE
                        );

                if (cm != null) {
                    cm.setPrimaryClip(
                            ClipData.newPlainText("dua", text)
                    );
                }
            });
        }

        @JavascriptInterface
        public void shareText(final String text) {

            runOnUiThread(() -> {

                Intent send = new Intent(
                        Intent.ACTION_SEND
                );

                send.setType("text/plain");

                send.putExtra(
                        Intent.EXTRA_TEXT,
                        text
                );

                startActivity(
                        Intent.createChooser(
                                send,
                                "دعا شیئر کریں"
                        )
                );
            });
        }
    }

    // ==================================================
    // CONSENT MANAGEMENT (UMP)
    // ==================================================

    private void startConsentThenAds() {

        try {

            ConsentInformation info =
                    UserMessagingPlatform.getConsentInformation(this);

            ConsentRequestParameters params =
                    new ConsentRequestParameters.Builder().build();

            info.requestConsentInfoUpdate(
                    this,
                    params,

                    () -> UserMessagingPlatform
                            .loadAndShowConsentFormIfRequired(
                                    this,
                                    error -> {

                                        if (info.canRequestAds()) {
                                            startAds();
                                        }
                                    }
                            ),

                    error -> {

                        if (info.canRequestAds()) {
                            startAds();
                        }
                    }
            );

            // Previously obtained consent
            if (info.canRequestAds()) {
                startAds();
            }

        } catch (Throwable t) {

            // App continues if advertising is unavailable.
        }
    }

    // ==================================================
    // ADMOB INITIALIZATION
    // ==================================================

    private void startAds() {

        if (!adsStarted.compareAndSet(false, true)) {
            return;
        }

        try {

            MobileAds.initialize(
                    this,
                    status -> { }
            );

            loadBanner();

        } catch (Throwable t) {

            // Advertising is optional.
        }
    }

    // ==================================================
    // ADAPTIVE BANNER AD
    // ==================================================

    private void loadBanner() {

        try {

            DisplayMetrics dm =
                    getResources().getDisplayMetrics();

            int widthDp =
                    (int) (dm.widthPixels / dm.density);

            if (widthDp <= 0) {
                return;
            }

            adView = new AdView(this);

            // Production or Debug ID from build.gradle
            adView.setAdUnitId(
                    BuildConfig.ADMOB_BANNER_ID
            );

            AdSize adSize =
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            this,
                            widthDp
                    );

            adView.setAdSize(adSize);

            adView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {

                    adContainer.setVisibility(
                            View.VISIBLE
                    );
                }

                @Override
                public void onAdFailedToLoad(
                        LoadAdError error
                ) {

                    adContainer.setVisibility(
                            View.GONE
                    );
                }
            });

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER
                    );

            adContainer.addView(
                    adView,
                    params
            );

            adView.loadAd(
                    new AdRequest.Builder().build()
            );

        } catch (Throwable t) {

            adContainer.setVisibility(View.GONE);
        }
    }

    // ==================================================
    // ACTIVITY LIFECYCLE
    // ==================================================

    @Override
    protected void onPause() {

        if (adView != null) {
            adView.pause();
        }

        if (webView != null) {
            webView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (adView != null) {
            adView.resume();
        }

        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {

        if (adView != null) {
            adContainer.removeView(adView);
            adView.destroy();
            adView = null;
        }

        if (webView != null) {
            webView.removeJavascriptInterface("Android");
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}

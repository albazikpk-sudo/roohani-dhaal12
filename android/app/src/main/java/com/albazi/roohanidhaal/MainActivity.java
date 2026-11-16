package com.albazi.roohanidhaal;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hosts the fully offline web app (assets/www) in a WebView.
 * Ads live in a native strip BELOW the WebView, so they can never cover the dua text
 * or the Copy/Share buttons. If ads fail or are disabled the app works unchanged.
 */
public class MainActivity extends AppCompatActivity {

    private static final String START_URL = "https://appassets.androidplatform.net/assets/www/index.html";
    private static final long INTERSTITIAL_MIN_GAP_MS = 4 * 60 * 1000L;

    private WebView webView;
    private FrameLayout adContainer;
    private AdView adView;
    private InterstitialAd interstitial;
    private long lastInterstitialShown = System.currentTimeMillis(); // no ad right after launch
    private final AtomicBoolean adsStarted = new AtomicBoolean(false);

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.color.green);

        webView = new WebView(this);
        root.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        adContainer = new FrameLayout(this);
        adContainer.setBackgroundColor(0xFFFFFFFF);
        adContainer.setVisibility(View.GONE); // shown only once an ad has actually loaded
        root.addView(adContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });

        setupWebView();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                webView.evaluateJavascript("(function(){return window.__rdBack ? window.__rdBack() : false;})()", value -> {
                    if (!"true".equals(value)) { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); }
                });
            }
        });

        webView.loadUrl(START_URL);
        startConsentThenAds();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // favourites / settings persist across launches
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setTextZoom(100);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setBackgroundColor(0xFFF7F5EE);
        webView.addJavascriptInterface(new Bridge(), "Android");
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Everything is local; never navigate away from the app.
                return !"appassets.androidplatform.net".equals(request.getUrl().getHost());
            }
        });
    }

    /** JavaScript -> native functions. Only these three are exposed. */
    public class Bridge {
        @JavascriptInterface public void copyText(final String text) {
            runOnUiThread(() -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("dua", text));
            });
        }
        @JavascriptInterface public void shareText(final String text) {
            runOnUiThread(() -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(send, "دعا شیئر کریں"));
            });
        }
        @JavascriptInterface public void maybeShowInterstitial() {
            runOnUiThread(MainActivity.this::showInterstitialIfAllowed);
        }
    }

    // ---------------- Ads (consent first, never blocks the app) ----------------

    private void startConsentThenAds() {
        try {
            ConsentInformation info = UserMessagingPlatform.getConsentInformation(this);
            ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
            info.requestConsentInfoUpdate(this, params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(this, err -> {
                    if (info.canRequestAds()) startAds();
                }),
                err -> { if (info.canRequestAds()) startAds(); });
            if (info.canRequestAds()) startAds();
        } catch (Throwable t) { /* ads are optional */ }
    }

    private void startAds() {
        if (!adsStarted.compareAndSet(false, true)) return;
        try {
            MobileAds.initialize(this, status -> { });
            loadBanner();
            loadInterstitial();
        } catch (Throwable t) { /* ads are optional */ }
    }

    private void loadBanner() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int widthDp = (int) (dm.widthPixels / dm.density);
        adView = new AdView(this);
        adView.setAdUnitId(BuildConfig.ADMOB_BANNER_ID);
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, widthDp));
        adView.setAdListener(new AdListener() {
            @Override public void onAdLoaded() { adContainer.setVisibility(View.VISIBLE); }
            @Override public void onAdFailedToLoad(LoadAdError e) { adContainer.setVisibility(View.GONE); }
        });
        adContainer.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadInterstitial() {
        InterstitialAd.load(this, BuildConfig.ADMOB_INTERSTITIAL_ID, new AdRequest.Builder().build(),
            new InterstitialAdLoadCallback() {
                @Override public void onAdLoaded(InterstitialAd ad) { interstitial = ad; }
                @Override public void onAdFailedToLoad(LoadAdError e) { interstitial = null; }
            });
    }

    private void showInterstitialIfAllowed() {
        if (interstitial == null) return;
        if (System.currentTimeMillis() - lastInterstitialShown < INTERSTITIAL_MIN_GAP_MS) return;
        final InterstitialAd ad = interstitial;
        interstitial = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() { lastInterstitialShown = System.currentTimeMillis(); loadInterstitial(); }
            @Override public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError e) { loadInterstitial(); }
        });
        ad.show(this);
    }

    @Override protected void onPause() { if (adView != null) adView.pause(); super.onPause(); }
    @Override protected void onResume() { super.onResume(); if (adView != null) adView.resume(); }
    @Override protected void onDestroy() {
        if (adView != null) adView.destroy();
        if (webView != null) { webView.removeJavascriptInterface("Android"); webView.destroy(); }
        super.onDestroy();
    }
}

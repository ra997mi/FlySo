package com.proximadev.flyso.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.proximadev.flyso.R;
import com.proximadev.flyso.services.Connectivity;
import com.proximadev.flyso.utils.ThemeUtils;

import es.dmoral.toasty.Toasty;

public class BrowserActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeView;
    private WebView mWebView;
    private String mUrl;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(prefs.getBoolean("RotationLock",true))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (prefs.getBoolean("FullScreen",false))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ThemeUtils.setTheme(this, prefs);
        setContentView(R.layout.activity_browser);
        mToolbar = findViewById(R.id.toolbar_browser);
        setSupportActionBar(mToolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        mWebView = findViewById(R.id.browser);
        setWebViewSettings();

        mSwipeView = findViewById(R.id.browser_swipelayout);
        mSwipeView.setColorSchemeResources(android.R.color.white);
        mSwipeView.setProgressBackgroundColorSchemeColor(ThemeUtils.getColor(this));
        mSwipeView.setOnRefreshListener(() -> mWebView.reload());

        mUrl = getIntent().getStringExtra("mUrl");

        if (mUrl != null && !mUrl.isEmpty()) {
            mWebView.loadUrl(mUrl);
        }
        else {
            Toasty.error(this, getString(R.string.bad_link), 0).show();
            super.onBackPressed();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setWebViewSettings(){
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 19)
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        else
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        mWebView.setWebViewClient(new mWebViewClient());

        mWebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try{
                String mFileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("FlySo", mFileName);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        });

        mWebView.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_BACK) && event.getAction() == MotionEvent.ACTION_UP && mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_browser, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_copy:
                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(getString(R.string.app_name), mUrl);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.browser_swipelayout), getResources().getString(R.string.copied_to_clipboard), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                } catch (Exception ignore){}

                return true;
            case R.id.ic_share:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                i.putExtra(Intent.EXTRA_TEXT, mUrl);
                startActivity(Intent.createChooser(i, getString(R.string.share_via)));
                return true;

            case R.id.ic_open:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl)));
                return true;

            case android.R.id.home:
                super.onBackPressed();
                return true;

            case R.id.ic_up:
                ObjectAnimator anim = ObjectAnimator.ofInt(mWebView, "scrollY", mWebView.getScrollY(), 0);
                anim.setDuration(400);
                anim.start();
                return true;
        }
        return false;
    }

    private class mWebViewClient extends WebViewClient {

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            try {
                if (!Connectivity.isConnected(BrowserActivity.this)) {
                    mWebView.loadUrl("file:///android_asset/error.html");
                    Snackbar snackbar = Snackbar.make(mWebView, R.string.connection, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.reload, v -> {
                        if (mWebView.canGoBack()) {
                            mWebView.stopLoading();
                            mWebView.goBack();
                        }
                    });
                    snackbar.show();
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mSwipeView.setRefreshing(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mSwipeView.setRefreshing(false);
            if (mToolbar != null)
                mToolbar.setTitle(view.getTitle());
        }
    }
}

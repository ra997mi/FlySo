package com.proximadev.flyso.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.proximadev.flyso.R;
import com.proximadev.flyso.activities.BrowserActivity;
import com.proximadev.flyso.services.Connectivity;
import com.proximadev.flyso.utils.ThemeUtils;

public class TwitterFragment extends BaseFragment {

    public TwitterFragment() {
        // Required empty public constructor
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_twitter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mSwipeView = view.findViewById(R.id.swiperefresh);
        mSwipeView.setColorSchemeResources(android.R.color.white);
        mSwipeView.setProgressBackgroundColorSchemeColor(ThemeUtils.getColor(mActivity));
        mSwipeView.setOnRefreshListener(() -> mWebView.reload());
        mSwipeView.setEnabled(prefs.getBoolean("SwipeToRefresh", true));

        mWebView = view.findViewById(R.id.TwitterWebView);
        mWebView.setWebViewClient(new mWebViewClient());
        mWebView.setWebChromeClient(new mWebChromeClient());
        setWebViewSettings();
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.getSettings().setUseWideViewPort(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        mWebView.setOnLongClickListener(v -> {
            try {
                String url = mWebView.getUrl();
                if (url.contains("/photo/") && url.contains("/status/")){
                    mWebView.loadUrl("javascript:(function() { alert(document.getElementsByTagName('img')[0].src) })()");
                }
                else if (url.contains("/video/") && url.contains("/status/")){
                    mWebView.loadUrl("javascript:(function() { alert(document.getElementsByTagName('video')[0].src) })()");
                }
            } catch (Exception ignore){
                return false;
            }
            return false;
        });

        mWebView.loadUrl("https://mobile.twitter.com");
    }

    private class mWebViewClient extends WebViewClient {

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            try {
                if (!Connectivity.isConnected(mActivity)) {
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
        }

        @SuppressLint("NewApi")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                if (Uri.parse(url).getHost().endsWith("twitter.com") || Uri.parse(url).getHost().endsWith("mobile.twitter.com")){
                    return false;
                } else {
                    if (prefs.getBoolean("AppBrowser", false)){
                        Intent intent = new Intent(mActivity, BrowserActivity.class);
                        intent.putExtra("mUrl", url);
                        startActivity(intent);
                    }
                    else
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
                return true;
            } catch (Exception ignore){
                return true;
            }
        }
    }
}

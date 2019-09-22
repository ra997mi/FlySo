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
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.proximadev.flyso.R;
import com.proximadev.flyso.activities.BrowserActivity;
import com.proximadev.flyso.others.JavaScriptHelpers;
import com.proximadev.flyso.services.Connectivity;
import com.proximadev.flyso.utils.ThemeUtils;

public class InstagramFragment extends BaseFragment {

    private String mInstagramTheme;

    public InstagramFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_instagram, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mSwipeView = view.findViewById(R.id.swiperefresh);
        mSwipeView.setColorSchemeResources(android.R.color.white);
        mSwipeView.setProgressBackgroundColorSchemeColor(ThemeUtils.getColor(mActivity));
        mSwipeView.setOnRefreshListener(() -> mWebView.reload());
        mSwipeView.setEnabled(prefs.getBoolean("SwipeToRefresh", true));

        mWebView = view.findViewById(R.id.InstagramWebView);
        mWebView.setWebViewClient(new mWebViewClient());
        mWebView.setWebChromeClient(new mWebChromeClient());
        setWebViewSettings();

        mWebView.setOnLongClickListener(v -> {
            try {
                String url = mWebView.getUrl();
                if (url.contains("https://www.instagram.com/stories/")) {
                    mWebView.loadUrl("javascript:(function() { alert(document.getElementsByClassName('y-yJ5')[0].src) })()");
                    mWebView.loadUrl("javascript:(function() { alert(document.getElementsByClassName('y-yJ5  OFkrO')[0].currentSrc) })()");
                } else if (url.contains("instagram.com/p/")) {
                    mWebView.loadUrl("javascript:(function() { alert(document.getElementsByClassName('KL4Bh')[0].getElementsByClassName('FFVAD')[0].src) })()");
                    mWebView.loadUrl("javascript:(function() { alert(document.getElementsByClassName('_5wCQW')[0].getElementsByClassName('tWeCl')[0].src) })()");
                } else if (!url.equalsIgnoreCase("https://www.instagram.com/")
                        && !url.contains("/accounts/")
                        && !url.contains("/about/")
                        && !url.contains("/explore/")
                        && !url.contains("/p/")
                        && !url.contains("/_u/")
                        && !url.contains("/followers/")
                        && !url.contains("/following/")
                        && !url.contains("/activity/")
                        && !url.contains("/settings/")
                        && !url.contains("help.")
                        && !url.contains("blog.")
                        && !url.contains("business.")
                        && !url.contains("press.com")
                        && !url.contains("facebook.com")
                        && !url.contains("www.instagram.com/story/")
                        && !url.contains("www.instagram.com/create/story/")
                        && !url.contains("www.instagram.com/stories/")
                        && !url.contains("www.instagram.com/create/style/")
                        && !url.contains("www.instagram.com/create/details/")
                        && !url.contains("/developer")
                        && !url.contains("/about/jobs/")) {
                    mWebView.loadUrl("javascript:(function() { alert(document.getElementsByClassName('_2dbep')[0].getElementsByClassName('_6q-tv')[0].src) })()");
                    mWebView.loadUrl("javascript:(function() { alert(document.getElementsByClassName('IalUJ')[0].getElementsByClassName('be6sR')[0].src) })()");
                }
            } catch (Exception ex){
                return false;
            }
            return false;
        });

        mWebView.loadUrl("https://www.instagram.com");

        mInstagramTheme = prefs.getString("instagram_theme", "instagram.css");
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

            JavaScriptHelpers.injectCSSFile(mActivity, view,  mInstagramTheme);
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
                if (Uri.parse(url).getHost().endsWith("instagram.com")
                        || Uri.parse(url).getHost().endsWith("facebook.com")){
                    return false;
                }

                else {
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
                return  true;
            }
        }
    }
}



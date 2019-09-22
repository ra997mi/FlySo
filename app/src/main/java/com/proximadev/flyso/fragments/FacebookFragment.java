package com.proximadev.flyso.fragments;

import android.animation.ObjectAnimator;
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
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.clans.fab.FloatingActionMenu;
import com.proximadev.flyso.R;
import com.proximadev.flyso.activities.BrowserActivity;
import com.proximadev.flyso.activities.PhotoActivity;
import com.proximadev.flyso.activities.VideoActivity;
import com.proximadev.flyso.others.Helpers;
import com.proximadev.flyso.others.JavaScriptHelpers;
import com.proximadev.flyso.services.Connectivity;
import com.proximadev.flyso.utils.ThemeUtils;
import com.proximadev.flyso.utils.Utility;

public class FacebookFragment extends BaseFragment {

    private FloatingActionMenu mMenuFAB;
    private String mFacebookTheme;

    public FacebookFragment() {
        // Required empty public constructor
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.facebook_fragment, container, false);
    }

    @SuppressLint("AddJavascriptInterface")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mSwipeView = view.findViewById(R.id.swiperefresh);
        mSwipeView.setColorSchemeResources(android.R.color.white);
        mSwipeView.setProgressBackgroundColorSchemeColor(ThemeUtils.getColor(mActivity));
        mSwipeView.setOnRefreshListener(() -> mWebView.reload());
        mSwipeView.setEnabled(prefs.getBoolean("SwipeToRefresh", true));

        mWebView = view.findViewById(R.id.FacebookWebView);
        mWebView.setWebViewClient(new mWebViewClient());
        mWebView.setWebChromeClient(new mWebChromeClient());
        setWebViewSettings();
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);


        mWebView.setOnLongClickListener(view1 -> {
            try{
                String url = mWebView.getUrl();
                if (url != null) {
                    if (url.contains("/photo.php?") || url.contains("/photos/")) {
                        mWebView.loadUrl("javascript:(function() {var img = document.querySelector(\"a[href*='.jpg']\");if (img != null){alert(img.getAttribute(\"href\"));}else {img = document.querySelector(\"i.img[data-sigil*='photo-image']\");if (img != null) {alert(img.getAttribute(\"style\"));}}})()");
                    }
                    else if (url.contains("/stories/view_tray/")) {
                        mWebView.loadUrl("javascript:(function() { alert(document.getElementsByClassName(\"_2b-9\")[0].getElementsByClassName(\"picture img\")[0].src) })()");
                    }
                }
            } catch (Exception ignore){
                return false;
            }
            return false;
        });

        mWebView.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux ARM) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
        mWebView.addJavascriptInterface(this, "Vid");

        // Inflate the FAB menu
        mMenuFAB = view.findViewById(R.id.menuFAB);

        View.OnClickListener mFABClickListener = v -> {
            switch (v.getId()) {
                case R.id.profileFAB:
                    mWebView.loadUrl("https://m.facebook.com/me");
                    break;
                case R.id.savedFAB:
                    mWebView.loadUrl("https://m.facebook.com/saved/");
                    break;
                case R.id.messagesFAB:
                    mWebView.loadUrl("https://m.facebook.com/messages/");
                    break;
                case R.id.topFAB:
                    ObjectAnimator anim = ObjectAnimator.ofInt(mWebView, "scrollY", mWebView.getScrollY(), 0);
                    anim.setDuration(400);
                    anim.start();
                    break;
                case R.id.shareFAB:
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, mWebView.getUrl());
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.context_share_link)));
                    break;
                case R.id.refreshsFAB:
                    mWebView.reload();
                    mWebView.requestFocus();
                    break;
                default:
                    break;
            }
            mMenuFAB.close(true);
        };

        if (prefs.getBoolean("facebookFab", false)) {
            mMenuFAB.setVisibility(View.VISIBLE);
        } else
            mMenuFAB.setVisibility(View.GONE);

        view.findViewById(R.id.profileFAB).setOnClickListener(mFABClickListener);
        view.findViewById(R.id.savedFAB).setOnClickListener(mFABClickListener);
        view.findViewById(R.id.messagesFAB).setOnClickListener(mFABClickListener);
        view.findViewById(R.id.topFAB).setOnClickListener(mFABClickListener);
        view.findViewById(R.id.shareFAB).setOnClickListener(mFABClickListener);
        view.findViewById(R.id.refreshsFAB).setOnClickListener(mFABClickListener);

        if (prefs.getBoolean("save_data", false)) {
            mWebView.loadUrl("https://mbasic.facebook.com");
            mMenuFAB.setVisibility(View.GONE);
        } else
            mWebView.loadUrl(prefs.getString("start_url", "https://m.facebook.com/home.php?sk=h_chr"));

        mFacebookTheme = prefs.getString("facebook_theme", "Classic");
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
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);

            if (!prefs.getBoolean("DefaultPlayer",false)) {
                JavaScriptHelpers.videoView(view);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mSwipeView.setRefreshing(false);

            // Enable or disable FAB
            if (url.contains("messages")){
                if (prefs.getBoolean("facebookFab", false)){
                    mMenuFAB.hideMenu(true);
                }
            }
            else {
                if (prefs.getBoolean("facebookFab", false)){
                    mMenuFAB.showMenu(true);
                }
            }

            if (!mFacebookTheme.equals("Classic")){
                JavaScriptHelpers.injectCSSFile(mActivity, view, mFacebookTheme);
            }

            if (prefs.getBoolean("facebook_center_text",false))
                JavaScriptHelpers.injectCSSFile(mActivity, view,  "facebook_center_text.css");
            if (prefs.getBoolean("facebook_copy_text",false))
                JavaScriptHelpers.injectCSSFile(mActivity, view, "text.css");

        }

        @SuppressLint("NewApi")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                // clean an url from facebook redirection before processing (no more blank pages on back)
                if (url != null)
                    url = Helpers.cleanAndDecodeUrl(url);

                if ((Uri.parse(url).getHost().endsWith("facebook.com"))) {
                    return false;
                }

                if (url != null && (url.contains("fbcdn.net") || url.contains("akamaihd"))) {

                    if (prefs.getBoolean("save_data", false) && url.contains("video")) {
                            LoadVideo(url);
                    } else
                        LoadPicture(url, view);
                    return true;
                }

                if (prefs.getBoolean("AppBrowser", false)){
                    Intent intent = new Intent(mActivity, BrowserActivity.class);
                    intent.putExtra("mUrl", url);
                    startActivity(intent);
                }

                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                return true;
            } catch (Exception ignore) {
                return true;
            }
        }
    }

    private void LoadPicture(String url, WebView view) {
        Intent Photo = new Intent(mActivity, PhotoActivity.class);
        Photo.putExtra("PictureUrl", url);
        Photo.putExtra("PictureName", Utility.getPictureName(url));
        Photo.putExtra("PictureTitle", view.getTitle());
        startActivity(Photo);
        view.stopLoading();
        view.goBack();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void LoadVideo(final String video_url) {
        Intent Video = new Intent(mActivity, VideoActivity.class);
        Video.putExtra("VideoUrl", video_url);
        Video.putExtra("VideoName", Utility.getVideoName(video_url));
        startActivity(Video);
    }
}


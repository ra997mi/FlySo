package com.proximadev.flyso.fragments;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.proximadev.flyso.R;
import com.proximadev.flyso.activities.MainActivity;
import com.proximadev.flyso.activities.PhotoActivity;
import com.proximadev.flyso.activities.VideoActivity;
import com.proximadev.flyso.others.Helpers;
import com.proximadev.flyso.utils.Utility;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import static android.content.Context.DOWNLOAD_SERVICE;


public abstract class BaseFragment extends Fragment{

    protected SwipeRefreshLayout mSwipeView;
    protected WebView mWebView;
    protected MainActivity mActivity;
    private String mCameraM;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageA;
    protected SharedPreferences prefs;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT < 21) {
            if (requestCode == 1 && this.mUploadMessage != null) {
                Uri result = (intent == null || resultCode != -1) ? null : intent.getData();
                this.mUploadMessage.onReceiveValue(result);
                this.mUploadMessage = null;
            }
        } else {
            if (requestCode != 1 || this.mUploadMessageA == null) {
                super.onActivityResult(requestCode, resultCode, intent);
                return;
            }
            Uri[] results = null;
            if (resultCode == -1) {
                if (intent != null) {
                    String dataString = intent.getDataString();
                    if (dataString != null) {
                        results = new Uri[1];
                        results[0] = Uri.parse(dataString);
                    }
                } else if (this.mCameraM != null) {
                    results = new Uri[1];
                    results[0] = Uri.parse(this.mCameraM);
                }
            }
            this.mUploadMessageA.onReceiveValue(results);
            this.mUploadMessageA = null;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            if (mActivity == null && context instanceof MainActivity) {
                mActivity = (MainActivity) context;
                prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
            }
        }
        catch (Exception ignore){}
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mActivity = null;
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    void setWebViewSettings() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBlockNetworkImage(prefs.getBoolean("SaveData", false));
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setGeolocationEnabled(prefs.getBoolean("location",false));
        webSettings.setAppCacheEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        if (Build.VERSION.SDK_INT >= 19)
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        else
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        webSettings.setTextZoom(Integer.parseInt(prefs.getString("TextSize", "100")));

        mWebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> mDownloadManager(url, URLUtil.guessFileName(url, contentDisposition, mimetype)));

        mWebView.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_BACK) && event.getAction() == MotionEvent.ACTION_UP && mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
            return false;
        });
    }

    private void mDownloadManager(@NonNull String mUrl, @NonNull String mName){
        try {
            if (mActivity.isPermissionsGranted()) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUrl));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir("FlySo", mName);
                DownloadManager dm = (DownloadManager) mActivity.getSystemService(DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                }
                Toasty.success(mActivity, getString(R.string.downloading), 0).show();
            }
            else
                Toasty.error(mActivity, getString(R.string.permission),0).show();

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public class mWebChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            try{
                if (message != null && !message.contains("undefined")){
                    if (message.contains("background-image:")) {
                        String mUrl = message.substring(message.indexOf("(") + 1, message.lastIndexOf(")"));
                        mUrl = Helpers.decodeImg(mUrl);
                        Intent Photo = new Intent(mActivity, PhotoActivity.class);
                        Photo.putExtra("PictureUrl", mUrl);
                        Photo.putExtra("PictureName", Utility.getPictureName(mUrl));
                        Photo.putExtra("PictureTitle", view.getTitle());
                        startActivity(Photo);
                    }

                    else if (message.contains("jpg") && !message.contains("mp4")){
                        Intent Photo = new Intent(mActivity, PhotoActivity.class);
                        Photo.putExtra("PictureUrl", message);
                        Photo.putExtra("PictureName", Utility.getPictureName(message));
                        Photo.putExtra("PictureTitle", view.getTitle());
                        startActivity(Photo);
                    }
                    else if ((message.contains("mp4") || message.contains("m3u8")) && !message.contains("jpg")){
                        Intent i = new Intent(mActivity, VideoActivity.class);
                        i.putExtra("VideoUrl", message);
                        i.putExtra("VideoName", Utility.getVideoName(message));
                        startActivity(i);
                    }
                }
            } catch (Exception ignore){
                return true;
            }
            result.confirm();
            return true;
        }

        @SuppressWarnings("unused")
        public void onSelectionStart(WebView view) {
            // Parent class aborts the selection, which seems like a terrible default.
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            // origin, allow, remember
            callback.invoke(origin, true, false);
        }

        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent("android.intent.action.GET_CONTENT");
            i.addCategory("android.intent.category.OPENABLE");
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), 1);
        }

        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent("android.intent.action.GET_CONTENT");
            i.addCategory("android.intent.category.OPENABLE");
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i, "File Browser"), 1);
        }

        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent("android.intent.action.GET_CONTENT");
            i.addCategory("android.intent.category.OPENABLE");
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), 1);
        }

        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            Intent[] intentArray;
            if (mUploadMessageA != null) {
                mUploadMessageA.onReceiveValue(null);
            }
            mUploadMessageA = filePathCallback;
            Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                File file = null;
                try {
                    file = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraM);
                } catch (Exception ignore) {}
                if (file != null) {
                    mCameraM = "file:" + file.getAbsolutePath();
                    takePictureIntent.putExtra("output", Uri.fromFile(file));
                } else {
                    takePictureIntent = null;
                }
            }
            Intent contentSelectionIntent = new Intent("android.intent.action.GET_CONTENT");
            contentSelectionIntent.addCategory("android.intent.category.OPENABLE");
            contentSelectionIntent.setType("*/*");
            if (takePictureIntent != null) {
                intentArray = new Intent[1];
                intentArray[0] = takePictureIntent;
            } else {
                intentArray = new Intent[0];
            }
            Intent chooserIntent = new Intent("android.intent.action.CHOOSER");
            chooserIntent.putExtra("android.intent.extra.INTENT", contentSelectionIntent);
            chooserIntent.putExtra("android.intent.extra.TITLE", "Choose a file for upload");
            chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", intentArray);
            startActivityForResult(chooserIntent, 1);
            return true;
        }
    }

    // Create an image file
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File createImageFile() throws IOException{
        File PictureDir = mActivity.getExternalCacheDir();
        File FlySoTemp = new File(PictureDir, "FlySoTemp");
        if (!FlySoTemp.exists())
            FlySoTemp.mkdir();
        return File.createTempFile("JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + "_", ".jpg", FlySoTemp);
    }
}
package com.proximadev.flyso.others;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import java.io.InputStream;

public class JavaScriptHelpers {

    public static void videoView(WebView view) {
        view.loadUrl("javascript:(function prepareVideo() { var el = document.querySelectorAll('div[data-sigil]');for(var i=0;i<el.length; i++){var sigil = el[i].dataset.sigil;if(sigil.indexOf('inlineVideo') > -1){delete el[i].dataset.sigil;console.log(i);var jsonData = JSON.parse(el[i].dataset.store);el[i].setAttribute('onClick', 'Vid.LoadVideo(\"'+jsonData['src']+'\");');}}})()");
        view.loadUrl("javascript:( window.onload=prepareVideo;)()");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void injectCSSFile(Activity context, WebView webView, String cssFileName) {
            try {
                if (context != null && webView != null && cssFileName != null) {
                    InputStream inputStream = context.getAssets().open(cssFileName);
                    byte[] buffer = new byte[inputStream.available()];
                    inputStream.read(buffer);
                    inputStream.close();
                    webView.loadUrl("javascript:(function() {" +
                            "var parent = document.getElementsByTagName('head').item(0);" +
                            "var style = document.createElement('style');" +
                            "style.type = 'text/css';" +
                            "style.innerHTML = window.atob('" + Base64.encodeToString(buffer, Base64.NO_WRAP) + "');" +
                            "parent.appendChild(style)" + "})()");
                }
            } catch (Exception ex) {
                Log.e("JavaScriptHelpers", "injectCSSFile: Error Happens!: ", ex);
                ex.printStackTrace();
            }
    }
}
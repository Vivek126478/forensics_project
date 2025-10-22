package com.example.forensics_project;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebAsteroidActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        // Fixed: JavaScript enabled for game to work
        settings.setJavaScriptEnabled(false);

        String url = "file:///android_asset/asteroid/index.html";
        webView.loadUrl(url);
    }
}



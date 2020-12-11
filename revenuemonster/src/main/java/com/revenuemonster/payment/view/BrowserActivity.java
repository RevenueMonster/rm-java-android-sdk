package com.revenuemonster.payment.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.revenuemonster.payment.R;

public class BrowserActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_top);
        toolbar.setTitle("RM Checkout");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
            }
        });

        Bundle b = getIntent().getExtras();
        String url = b.getString("url");

        ImageView closeButton = (ImageView) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        webView = (WebView) findViewById(R.id.web_view);
        loadWebViewLoad(webView, url);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient()
        {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    try {
                        String url = view.getUrl();
                        if (url.contains("/v1/transaction/web/close")) {
                            finish();
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    private void loadWebViewLoad(WebView webview, String url) {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setSupportMultipleWindows(true);
        webview.setWebViewClient(new WebViewClient());
        webview.setWebChromeClient(new WebChromeClient());
        webview.loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

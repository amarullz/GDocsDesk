package com.amarullz.app.googledocs;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class DocsView {
  private final AppCompatActivity activity;
  private final String url;
  private final WebView webView;
  private String userAgent;
  private boolean onLogin =false;

  public void setUAG(boolean isLogin){
    if (onLogin !=isLogin) {
      onLogin =isLogin;
      if (onLogin){
        userAgent = "com.amarullz.apps.googledocs";
      }
      else {
        userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.61";
      }
      activity.runOnUiThread(()->webView.getSettings().setUserAgentString(userAgent));
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  public DocsView(AppCompatActivity act, String start_url) {
    activity=act;
    url=start_url;

//    activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    activity.setContentView(R.layout.activity_main);
    webView = activity.findViewById(R.id.webview);
    webView.setWebViewClient(new WebViewClient());
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    setUAG(true);

    CookieManager.getInstance().setAcceptCookie(true);
    CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    webSettings.setSupportMultipleWindows(false);
    webSettings.setDomStorageEnabled(true);
    webSettings.setUseWideViewPort(true);
    webSettings.setSaveFormData(true);
    webSettings.setUseWideViewPort(false);
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setMediaPlaybackRequiresUserGesture(false);
    webSettings.setAllowFileAccess(true);
    webSettings.setAllowContentAccess(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setDatabaseEnabled(true);
    webSettings.setDomStorageEnabled(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
      webSettings.setAlgorithmicDarkeningAllowed(true);
    webSettings.setGeolocationEnabled(true);
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    webView.setFocusableInTouchMode(true);
    webView.setFocusable(true);
    webView.setInitialScale(100);
    webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

    webView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
      }
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String ch_url=request.getUrl().toString();
        boolean docs_redirect=false;
        if (ch_url.contains("myaccount.google.com/general")){
          docs_redirect=true;
          ch_url=url;
        }
        final String req_url=ch_url;
        if (request.getUrl().getHost().contains("drive.google")||
            request.getUrl().getHost().contains("docs.google")||docs_redirect){
          if (onLogin) {
            setUAG(false);
            activity.runOnUiThread(()->webView.loadUrl(req_url));
            return true;
          }
        }
        else{
          if (!onLogin) {
            setUAG(true);
            activity.runOnUiThread(()->webView.loadUrl(req_url));
            return true;
          }
        }
        return false;
      }
    });
    webView.loadUrl(url);
  }
}

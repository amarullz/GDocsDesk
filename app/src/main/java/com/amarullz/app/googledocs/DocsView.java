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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

  /* Init Floating Action Buttons */
  private boolean fabOpened=false;
  private boolean isFullscreen=false;
  private int zoomSize=0;
  private final FloatingActionButton fab;
  private final FloatingActionButton fabMouse;
  private final FloatingActionButton fabZoomOut;
  private final FloatingActionButton fabZoomIn;
  private final FloatingActionButton fabFullscreen;

  private void fabShow(boolean show){
    if (show){
      fabMouse.animate().translationY(activity.getResources().getDimension(R.dimen.fab_move_mouse));
      fabZoomOut.animate().translationY(activity.getResources().getDimension(R.dimen.fab_move_zoomout));
      fabZoomIn.animate().translationY(activity.getResources().getDimension(R.dimen.fab_move_zoomin));
      fabFullscreen.animate().translationY(activity.getResources().getDimension(R.dimen.fab_move_fullscreen));

      fabMouse.animate().rotation(0);
      fabZoomOut.animate().rotation(0);
      fabZoomIn.animate().rotation(0);
      fabFullscreen.animate().rotation(0);
      fab.animate().rotation(90);
    }
    else{
      fabMouse.animate().translationY(0);
      fabZoomOut.animate().translationY(0);
      fabZoomIn.animate().translationY(0);
      fabFullscreen.animate().translationY(0);

      fabMouse.animate().rotation(90);
      fabZoomOut.animate().rotation(90);
      fabZoomIn.animate().rotation(90);
      fabFullscreen.animate().rotation(90);
      fab.animate().rotation(0);
    }
    fabOpened=show;
  }
  private void setFullscreen(boolean fullscreen){
    if (fullscreen){
      activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
      fabFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        fabFullscreen.setTooltipText(activity.getResources().getString(R.string.fab_fullscreen_exit));
      }
    }
    else{
      activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      fabFullscreen.setImageResource(R.drawable.ic_fullscreen);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        fabFullscreen.setTooltipText(activity.getResources().getString(R.string.fab_fullscreen));
      }
    }
    isFullscreen=fullscreen;
  }

  private Toast zoomToast=null;

  private void setZoom(int add){
    zoomSize+=add;
    if (zoomSize>6) zoomSize=6;
    else if (zoomSize<0) zoomSize=0;
    int zoomValue=100+(zoomSize*25);
    webView.setInitialScale(zoomValue);

    if (zoomToast!=null)
      zoomToast.cancel();
    zoomToast=Toast.makeText(activity, "Zoom "+zoomValue+"%", Toast.LENGTH_SHORT);
    zoomToast.show();
  }

  public void initFab(){
    /* Fullscreen Button */
    fabFullscreen.setOnClickListener(v->{
      setFullscreen(!isFullscreen);
      fabShow(false);
    });

    /* Zoom In & Out */
    fabZoomIn.setOnClickListener(v -> {
      setZoom(1);
    });
    fabZoomOut.setOnClickListener(v -> {
      setZoom(-1);
    });

    /* Fab Menu */
    fab.setOnClickListener(v -> {
      fabShow(!fabOpened);
    });
  }

  /* Docs View Constructor */
  @SuppressLint("SetJavaScriptEnabled")
  public DocsView(AppCompatActivity act, String start_url) {
    activity=act;
    url=start_url;

    /* Set Content View */
    activity.setContentView(R.layout.activity_main);

    /* Init Fabs */
    fab=activity.findViewById(R.id.fab);
    fabMouse=activity.findViewById(R.id.fab_mouse);
    fabZoomOut=activity.findViewById(R.id.fab_zoomout);
    fabZoomIn=activity.findViewById(R.id.fab_zoomin);
    fabFullscreen=activity.findViewById(R.id.fab_fullscreen);
    initFab();

    /* Init Webview */
    webView = activity.findViewById(R.id.webview);
    webView.setWebViewClient(new WebViewClient());
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    setUAG(true);

    /* WebView Settings */
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
    webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

    webView.setInitialScale(100);

    /* WebView Client */
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
      }
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        /* Change User-Agent based on Google URL */
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

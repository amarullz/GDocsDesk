package com.amarullz.app.googledocs;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;

public class DocsView {
  /* WebView Variables */
  private final AppCompatActivity activity;
  private final String url;
  private final WebView webView;
  private String userAgent;
  private final String userAgentLogin;
  private final String userAgentApps;
  private boolean onLogin =false;

  /* Cursor Related Variables */
  public final ImageView cursor;
  public final RelativeLayout webLayout;
  private PointerIcon pointerActive;
  private final ArrayList<PointerIcon> pointers;
  private int mouseX=0;
  private int mouseY=0;

  /* Settings & Floating Actions Variables */
  private boolean fabOpened=false;
  private boolean isFullscreen=false;
  private int zoomSize=0;
  private Toast zoomToast=null;
  private final FloatingActionButton fab;
  private final FloatingActionButton fabMouse;
  private final FloatingActionButton fabZoomOut;
  private final FloatingActionButton fabZoomIn;
  private final FloatingActionButton fabFullscreen;


  /* Show/hide fab menu */
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

  /* Set Fullscreen / Exit Fullscreen */
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

  /* Set WebView zoom */
  private void setZoom(int add){
    zoomSize+=add;
    if (zoomSize>6) zoomSize=6;
    else if (zoomSize<0) zoomSize=0;
    int zoomValue=100+(zoomSize*25);
    webView.setInitialScale(zoomValue);

    if (zoomToast!=null)
      zoomToast.cancel();

    String toastText=
        activity.getResources().getString(R.string.zoom_toast).replace("%",
            zoomValue+"%");
    zoomToast=Toast.makeText(activity, toastText, Toast.LENGTH_SHORT);
    zoomToast.show();
  }

  /* Init Floating Action Buttons */
  public void initFab(){
    /* Fullscreen Button */
    fabFullscreen.setOnClickListener(v->{
      setFullscreen(!isFullscreen);
      fabShow(false);
    });

    /* Zoom In & Out */
    fabZoomIn.setOnClickListener(v -> setZoom(1));
    fabZoomOut.setOnClickListener(v -> setZoom(-1));

    /* Fab Menu */
    fab.setOnClickListener(v -> fabShow(!fabOpened));
  }

  /* Set User Agents */
  public void setUAG(boolean isLogin){
    if (onLogin !=isLogin) {
      onLogin =isLogin;
      if (onLogin){
        userAgent = userAgentLogin;
      }
      else {
        userAgent = userAgentApps;
      }
      activity.runOnUiThread(()->webView.getSettings().setUserAgentString(userAgent));
    }
  }

  /* Docs View Constructor */
  @SuppressLint("SetJavaScriptEnabled")
  public DocsView(AppCompatActivity act, String start_url) {
    activity=act;
    url=start_url;

    /* Set Content View */
    activity.setContentView(R.layout.activity_main);

    /* Init FAB */
    fab=activity.findViewById(R.id.fab);
    fabMouse=activity.findViewById(R.id.fab_mouse);
    fabZoomOut=activity.findViewById(R.id.fab_zoomout);
    fabZoomIn=activity.findViewById(R.id.fab_zoomin);
    fabFullscreen=activity.findViewById(R.id.fab_fullscreen);
    initFab();

    /* Init WebView */
    webView = activity.findViewById(R.id.webview);
    webView.setWebViewClient(new WebViewClient());
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);

    /* Init User Agents */
    userAgentLogin = activity.getResources().getString(R.string.uag_login);
    userAgentApps = activity.getResources().getString(R.string.uag_apps);
    setUAG(true);

    /* Init Cursor Pointer */
    cursor=activity.findViewById(R.id.cursor);
    webLayout=activity.findViewById(R.id.weblayout);
    pointers = new ArrayList<>();
    pointers.add(PointerIcon.getSystemIcon(activity,PointerIcon.TYPE_DEFAULT));
    pointers.add(PointerIcon.getSystemIcon(activity,PointerIcon.TYPE_TEXT));
    pointers.add(PointerIcon.getSystemIcon(activity,PointerIcon.TYPE_HAND));
    pointers.add(PointerIcon.getSystemIcon(activity,PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW));
    pointers.add(PointerIcon.getSystemIcon(activity,PointerIcon.TYPE_VERTICAL_DOUBLE_ARROW));
    pointerActive=pointers.get(0);
    initVirtualMouse();

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
            request.getUrl().getHost().contains("docs.google")||
            docs_redirect){
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

  private void updatePointerIcon(){
    try {
      PointerIcon pi = webView.getPointerIcon();
      if (pi == null) return;
      if (!pi.equals(pointerActive)) {
        int pos = pointers.indexOf(pi);
        if (pos >= 0) {
          switch (pos) {
            case 1:
              cursor.setImageResource(R.drawable.cursor_text);
              break;
            case 2:
              cursor.setImageResource(R.drawable.cursor_hand);
              break;
            case 3:
              cursor.setImageResource(R.drawable.cursor_hsize);
              break;
            case 4:
              cursor.setImageResource(R.drawable.cursor_vsize);
              break;
            default:
              cursor.setImageResource(R.drawable.cursor_default);
              break;
          }
        }
        pointerActive = pi;
      }
    }catch (Exception ignored){}
  }
  private void setCursorPos(float x, float y){
    cursor.setTranslationX(x);
    cursor.setTranslationY(y);
    updatePointerIcon();
  }
  private void setCursorVisibility(boolean visible){
    cursor.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
  }
  private int dpx(float dp) {
    final float scale = activity.getResources().getDisplayMetrics().density;
    return (int) (dp * scale + 0.5f);
  }
  private void mouseEv(int type, float x, float y,
                      float scrollX, float scrollY, long elapsed){
    try {
      mouseX += dpx(x);
      mouseY += dpx(y);
      if (mouseX < 0) mouseX = 0;
      if (mouseY < 0) mouseY = 0;
      if (mouseX > webView.getWidth()) mouseX = webView.getWidth();
      if (mouseY > webView.getHeight()) mouseY = webView.getHeight();

      MotionEvent ev;
      long tick = new Date().getTime();
      long downTime = tick - elapsed;
      int metaState = 0;
      int buttonState = 0;
      byte actionButton = 0;
      int action = -1;

      MotionEvent.PointerCoords[] mPos = new MotionEvent.PointerCoords[1];
      mPos[0] = new MotionEvent.PointerCoords();
      mPos[0].x = mouseX;
      mPos[0].y = mouseY;
      mPos[0].pressure = 1;
      mPos[0].size = 0;

      MotionEvent.PointerProperties[] mProp = new MotionEvent.PointerProperties[1];
      mProp[0] = new MotionEvent.PointerProperties();
      mProp[0].id = 0;
      mProp[0].toolType = MotionEvent.TOOL_TYPE_MOUSE;

      if (type == 0) {
        // MOVE
        action = MotionEvent.ACTION_HOVER_MOVE;
      } else if (type == 1) {
        // MOUSE DOWN
        actionButton = MotionEvent.BUTTON_PRIMARY;
        action = MotionEvent.ACTION_BUTTON_PRESS;
        buttonState = 0x8000000;
      } else if (type == 2) {
        // MOUSE UP
        actionButton = MotionEvent.BUTTON_PRIMARY;
        action = MotionEvent.ACTION_BUTTON_RELEASE;
        buttonState = 0x8000000;
      } else if (type == 3) {
        // MOUSE DOWN SECONDARY
        actionButton = MotionEvent.BUTTON_SECONDARY;
        action = MotionEvent.ACTION_BUTTON_PRESS;
        buttonState = 0x8000000;
      } else if (type == 4) {
        // MOUSE UP SECONDARY
        actionButton = MotionEvent.BUTTON_SECONDARY;
        action = MotionEvent.ACTION_BUTTON_RELEASE;
        buttonState = 0x8000000;
      } else if (type == 5) {
        // DRAG
        action = MotionEvent.ACTION_MOVE;
        buttonState = MotionEvent.BUTTON_PRIMARY;
      } else if (type == 6) {
        // DRAG START
        action = MotionEvent.ACTION_DOWN;
        buttonState = MotionEvent.BUTTON_PRIMARY;
      } else if (type == 7) {
        // DRAG END
        action = MotionEvent.ACTION_UP;
        buttonState = MotionEvent.BUTTON_PRIMARY;
      } else if (type == 8) {
        // SCROLL
        action = MotionEvent.ACTION_SCROLL;
        mPos[0].setAxisValue(MotionEvent.AXIS_HSCROLL, scrollX / dpx(20));
        mPos[0].setAxisValue(MotionEvent.AXIS_VSCROLL, scrollY / dpx(20));
      }

      ev = MotionEvent.obtain(
          downTime, tick,
          action, 1, mProp, mPos,
          metaState, buttonState,
          1f, 1f, 0x19238347, 0,
          InputDevice.SOURCE_MOUSE, 0
      );

      if (actionButton == 1 || actionButton == 2) {
        Parcel p = Parcel.obtain();
        ev.writeToParcel(p, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        p.setDataPosition(0);
        ev.recycle();
        byte[] buf = p.marshall();
        buf[84] = buf[68] = actionButton;
        p.unmarshall(buf, 0, buf.length);
        p.setDataPosition(0);
        ev = MotionEvent.CREATOR.createFromParcel(p);
        p.recycle();
      }
      if (type >= 5 && type <= 7)
        webView.dispatchTouchEvent(ev);
      else
        webView.dispatchGenericMotionEvent(ev);
      // runOnUiThread(()->setCursorPos(mouseX, mouseY));

      setCursorPos(mouseX, mouseY);
    }catch (Exception ignored){}
  }

  private void initVirtualMouse(){

  }
}

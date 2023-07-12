package com.amarullz.app.googledocs;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DocsView {
  public static final String _TAG = "DOCVIEW";

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
  public final WebLayout webLayout;
  private PointerIcon pointerActive;
  private final ArrayList<PointerIcon> pointers;
  private float mouseX=0;
  private float mouseY=0;
  private boolean isCursorVisible=false;

  /* Settings & Floating Actions Variables */
  private boolean fabOpened=false;
  private boolean isFullscreen=false;
  private int zoomSize=1;
  private Toast zoomToast=null;
  private final FloatingActionButton fab;
  private final FloatingActionButton fabMouse;
  private final FloatingActionButton fabZoomOut;
  private final FloatingActionButton fabZoomIn;
  private final FloatingActionButton fabFullscreen;
  private final FloatingActionButton fabHome;
  private final FloatingActionButton fabBack;
  private final FloatingActionButton fabSoftkey;

  /* Helpers */
  private static long tick(){
    return System.currentTimeMillis();
  }

  /* Show/hide fab menu */
  private void fabShow(boolean show){
    if (show){
      fabMouse.animate().translationY(activity.getResources().getDimension(R.dimen.fab_move_mouse));
      fabZoomOut.animate().translationY(activity.getResources().getDimension(R.dimen.fab_move_zoomout));
      fabZoomIn.animate().translationY(activity.getResources().getDimension(R.dimen.fab_move_zoomin));
      fabFullscreen.animate().translationY(activity.getResources().getDimension(R.dimen.fab_move_fullscreen));

      fabHome.animate().translationX(activity.getResources().getDimension(R.dimen.fab_move_home));
      fabBack.animate().translationX(activity.getResources().getDimension(R.dimen.fab_move_back));


      fabMouse.animate().rotation(0);
      fabZoomOut.animate().rotation(0);
      fabZoomIn.animate().rotation(0);
      fabFullscreen.animate().rotation(0);
      fabHome.animate().rotation(0);
      fabBack.animate().rotation(0);
      fabSoftkey.animate().rotation(0);
      fab.animate().rotation(90);

      fabMouse.animate().alpha(1);
      fabZoomOut.animate().alpha(1);
      fabZoomIn.animate().alpha(1);
      fabFullscreen.animate().alpha(1);
      fabHome.animate().alpha(1);
      fabBack.animate().alpha(1);
      fabSoftkey.animate().alpha(1);
      fab.animate().alpha(1);
    }
    else{
      fabMouse.animate().translationY(0);
      fabZoomOut.animate().translationY(0);
      fabZoomIn.animate().translationY(0);
      fabFullscreen.animate().translationY(0);
      fabHome.animate().translationX(0);
      fabBack.animate().translationX(0);

      fabMouse.animate().rotation(90);
      fabZoomOut.animate().rotation(90);
      fabZoomIn.animate().rotation(90);
      fabFullscreen.animate().rotation(90);
      fabHome.animate().rotation(90);
      fabBack.animate().rotation(90);
      fabSoftkey.animate().rotation(0);
      fab.animate().rotation(0);

      fabMouse.animate().alpha(0);
      fabZoomOut.animate().alpha(0);
      fabZoomIn.animate().alpha(0);
      fabFullscreen.animate().alpha(0);
      fabHome.animate().alpha(0);
      fabBack.animate().alpha(0);
      fabSoftkey.animate().alpha(0.3f);
      fab.animate().alpha(0.3f);
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

  private boolean isSoftkeyVisible=false;
  private void setSoftKeyboard(boolean state){
    InputMethodManager manager = (InputMethodManager) activity.getSystemService( Context.INPUT_METHOD_SERVICE);
    if (state){
      manager.showSoftInput(webLayout, 0);
      fabSoftkey.setImageResource(R.drawable.ic_keyboard_hide);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        fabSoftkey.setTooltipText(activity.getResources().getString(R.string.fab_hide_keyboard));
      }
    }
    else{
      manager.hideSoftInputFromWindow(webLayout.getWindowToken(), 0);
      fabSoftkey.setImageResource(R.drawable.ic_keyboard);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        fabSoftkey.setTooltipText(activity.getResources().getString(R.string.fab_show_keyboard));
      }
    }
    isSoftkeyVisible=state;
  }

  /* Init Floating Action Buttons */
  private void initFab(){
    /* Fullscreen Button */
    fabFullscreen.setOnClickListener(v->{
      setFullscreen(!isFullscreen);
      fabShow(false);
    });

    fabSoftkey.setOnClickListener(v->{
      setSoftKeyboard(!isSoftkeyVisible);
    });

    /* Virtual Mouse */
    fabMouse.setOnClickListener(v->{
      setCursorVisibility(!isCursorVisible);
      initPointer(isCursorVisible);
      fabShow(false);
    });

    /* Zoom In & Out */
    fabZoomIn.setOnClickListener(v -> setZoom(1));
    fabZoomOut.setOnClickListener(v -> setZoom(-1));

    /* Navigation */
    fabHome.setOnClickListener(v->{
      webView.clearHistory();
      webView.loadUrl(url);
      fabShow(false);
    });
    fabBack.setOnClickListener(v->{
      if (webView.canGoBack())
        webView.goBack();
      fabShow(false);
    });

    /* Fab Menu */
    fab.setOnClickListener(v -> {
      fabShow(!fabOpened);
      vibrate(50);
    });
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
    fabHome=activity.findViewById(R.id.fab_home);
    fabBack=activity.findViewById(R.id.fab_back);
    fabSoftkey=activity.findViewById(R.id.fab_softkey);
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
    setCursorVisibility(false);
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

    webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

    webView.setInitialScale(125);

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
      @Override
      public void onPageFinished(WebView view, String url) {
        /* Add Clipboard Support & Tab Handling */
        super.onPageFinished(view, url);
        webView.evaluateJavascript("window.addEventListener('keydown',function" +
                "(e){console.log('CCLOG KEY = '+e.keyCode); if (e.keyCode==9){e" +
                ".preventDefault();" +
                "e" +
                ".stopPropagation();}});"
            ,null);
        webView.evaluateJavascript("navigator.clipboard.readText=function(){"+
                "return new Promise(resolve => {resolve(_DOCJSAPI.readClipboardText())" +
                "})"+
                "}"
            ,null);
      }
    });
    webView.addJavascriptInterface(new DocViewJsInterface(), "_DOCJSAPI");
    webView.loadUrl(url);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      webLayout.setOnCapturedPointerListener((view, event) -> {
        if (event.getAction()==MotionEvent.ACTION_MOVE){
          mouseEv(mouseBtnDown?5:0, dpx(event.getX()), dpx(event.getY()), 0, 0, 0);
        }
        else if (event.getAction()==MotionEvent.ACTION_BUTTON_PRESS){
          if (event.getActionButton()==MotionEvent.BUTTON_SECONDARY){
            mouseEv(3,0,0,0,0,0);
          }
          else{
            mouseBtnDown=true;
            mouseEv(1,0,0,0,0,0);
            mouseEv(6,0,0,0,0,0);
          }
        }
        else if (event.getAction()==MotionEvent.ACTION_BUTTON_RELEASE){
          if (event.getActionButton()==MotionEvent.BUTTON_SECONDARY){
            mouseEv(4,0,0,0,0,0);
          }
          else{
            mouseBtnDown=false;
            mouseEv(2,0,0,0,0,0);
            mouseEv(7,0,0,0,0,0);
          }
          Log.d("DOCSLOG", "ACTION BUTTON = " + event.getActionButton());
        }
        else if (event.getAction()==MotionEvent.ACTION_SCROLL){
          scrollTargetX+=event.getAxisValue(MotionEvent.AXIS_HSCROLL) * dpx(5);
          scrollTargetY+=event.getAxisValue(MotionEvent.AXIS_VSCROLL) * dpx(5);
          startFlingScroll();
        }
        return true;
      });
    }

    webView.setFocusableInTouchMode(false);
    webView.setFocusable(false);
    webLayout.setFocusableInTouchMode(true);
    webLayout.setFocusable(true);
    webLayout.requestFocus();
    setFullscreen(true);
    isCursorVisible=true;

    KeyboardVisibilityEvent.setEventListener(
    activity,
    isOpen -> {
      Log.d("DOCSLOG","ON KEYBOARD VIS = "+isOpen);
      setSoftKeyboard(isOpen);
    });

  }

  private Timer vmScrollFlingInterval=null;
  private float scrollTargetX=0;
  private float scrollTargetY=0;
  private class VmScrollFlingTask extends TimerTask{
    @Override
    public void run() {
      if (Math.abs(scrollTargetX)>=1||Math.abs(scrollTargetY)>=1){
        scrollTargetX=scrollTargetX*0.9f;
        scrollTargetY=scrollTargetY*0.9f;
        mouseEv(8,0,0,scrollTargetX,scrollTargetY,0);
      }
      else{
        scrollTargetX=scrollTargetY=0f;
        if (vmScrollFlingInterval!=null) {
          cancelTimer(vmScrollFlingInterval);
          vmScrollFlingInterval=null;
        }
      }
    }
  }
  private void startFlingScroll(){
    if (vmScrollFlingInterval==null) {
      vmScrollFlingInterval = new Timer();
      vmScrollFlingInterval.scheduleAtFixedRate(new VmScrollFlingTask(), 0, 16);
    }
  }

  private boolean mouseBtnDown=false;

  public void updateWindowFocus(){
    setCursorVisibility(isCursorVisible);
    initPointer(isCursorVisible);
  }
  private void initPointer(boolean active){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      mouseBtnDown=false;
      if (active) {
        Log.d("DOCSLOG", "WEBVIEW CLICK");
        webLayout.requestPointerCapture();
      }
      else{
        webLayout.releasePointerCapture();
      }
    }
  }


  public class DocViewJsInterface{
    @JavascriptInterface
    public String readClipboardText() {
      ClipboardManager clipboard =
          (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
      ClipData dat=clipboard.getPrimaryClip();
      if (dat.getItemCount()>0) {
        return (String) clipboard.getPrimaryClip().getItemAt(0).getText();
      }
      return null;
    }
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
    isCursorVisible=visible;

    if (visible) {
      fabMouse.setImageResource(R.drawable.ic_touch);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        fabMouse.setTooltipText(activity.getResources().getString(R.string.fab_touch));
      }
    }
    else{
      fabMouse.setImageResource(R.drawable.ic_mouse);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        fabMouse.setTooltipText(activity.getResources().getString(R.string.fab_mouse));
      }
    }
  }
  private int dpx(float dp) {
    final float scale = activity.getResources().getDisplayMetrics().density;
    return (int) (dp * scale + 0.5f);
  }
  private void mouseEv(int type, float x, float y,
                      float scrollX, float scrollY, long elapsed){
    try {
      mouseX += (x);
      mouseY += (y);
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
      setCursorPos(mouseX, mouseY);
    }catch (Exception ignored){}
  }

  /* Vibrating */
  private void vibrate(int duration){
    Vibrator vibrator =
        (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(duration,
          VibrationEffect.DEFAULT_AMPLITUDE));
    }
    else{
      vibrator.vibrate(duration);
    }
  }

  /* Virtual Mouse Variables */
  private boolean vmIs2Finger=false;
  private boolean vmIsScroll=false;
  private boolean vmDowned=false;
  private boolean vmFlingScroll=false;
  private boolean vmOnWaitClick=false;
  private boolean vmMoved=false;
  private float vmCurrX=0f;
  private float vmCurrY=0f;
  private float vmLastX=0f;
  private float vmLastY=0f;
  private float vmVelocityX=0f;
  private float vmVelocityY=0f;
  private long vmDownTick=0;
  private long vmDragTick=0;
  private Timer vmMoveInterval=null;
  private Timer vmFlingInterval=null;
  private Timer vmWaitClick=null;
  private class VmMoveTask extends TimerTask {
    @Override
    public void run() {
      if (vmDowned){
        if (!vmMoved&&!vmOnWaitClick&&!vmIsScroll&&!vmIs2Finger&&(tick()-vmDownTick>500)){
          vibrate(40);
          Log.d(_TAG,"RIGHT MOUSE DOWN - HOLD");
          mouseEv(3,0,0,0,0,0);
          Log.d(_TAG,"RIGHT MOUSE UP - HOLD");
          mouseEv(4,0,0,0,0,0);
          vmDowned=false;
          return;
        }
        if (vmCurrX!=0f||vmCurrY!=0f){
          if (!vmMoved){
            if (Math.abs(vmCurrX)+Math.abs(vmCurrY)>dpx(1)){
              vmMoved=true;
              if (vmIs2Finger||vmIsScroll){
                Log.d(_TAG,"SCROLLING..");
              }
              else{
                if (vmOnWaitClick){
                  vmDragTick=tick();
                  mouseEv(6,0,0,0,0,0);
                }
                Log.d(_TAG,"MOVING"+((vmOnWaitClick)?" - DRAG":"..."));
              }
            }
          }
          if (vmMoved){
            if (vmIs2Finger||vmIsScroll){
              mouseEv(8,0,0,vmCurrX,vmCurrY,0);
            }
            else{
              if (vmOnWaitClick){
                // MOVE DRAG
                mouseEv(5,vmCurrX,vmCurrY,0,0,
                    tick()-vmDownTick
                );
              }
              else{
                mouseEv(0,vmCurrX,vmCurrY,0,0,0);
              }
            }
            /* calculate fling velocity */
            if (Math.abs(vmCurrX)+Math.abs(vmCurrY)>dpx(1)){
              vmVelocityX=vmCurrX;
              vmVelocityY=vmCurrY;
            }
            else{
              vmVelocityX=0f;
              vmVelocityY=0f;
            }
            vmCurrY=vmCurrX=0;
          }
        }
      }
    }
  }
  private class VmFlingTask extends TimerTask{
    @Override
    public void run() {
      if (Math.abs(vmVelocityX)>=1||Math.abs(vmVelocityY)>=1){
        vmVelocityX=vmVelocityX*0.9f;
        vmVelocityY=vmVelocityY*0.9f;
        if (vmFlingScroll)
          mouseEv(8,0,0,vmVelocityX,vmVelocityY,0);
        else
          mouseEv(0,vmVelocityX,vmVelocityY,0,0,0);
      }
      else{
        Log.d(_TAG,"FLING END");
        vmVelocityX=vmVelocityY=0f;
        if (vmFlingInterval!=null) {
          cancelTimer(vmFlingInterval);
          vmFlingInterval=null;
        }
      }
    }
  }

  private class VmWaitClickTask extends TimerTask{
    @Override
    public void run() {
      vmOnWaitClick=false;
      Log.d(_TAG,"MOUSE UP");
      mouseEv(2,0,0,0,0,0);
      if (vmWaitClick!=null) {
        cancelTimer(vmWaitClick);
        vmWaitClick = null;
      }
    }
  }

  private void cancelTimer(Timer timer){
    try {
      timer.cancel();
    }catch (Exception ignored){}
  }

  private void initVirtualMouse(){
    /* Handle Key Event */
    webLayout.setKeyListener(ev -> {
      Log.d("DOCSLOG","KEYEV : "+ev);
      webView.dispatchKeyEvent(ev);
      return false;
    });
    /* Handle Touch Event */
    webLayout.setTouchListener(ev -> {
      /* Ignore it */
      if (!isCursorVisible) return false;

      switch(ev.getAction()){
        case MotionEvent.ACTION_DOWN:
          vmCurrX=vmCurrY=vmVelocityX=vmVelocityY=0;
          vmMoved=false;
          vmIs2Finger=(ev.getPointerCount()>1);
          vmDowned=true;
          vmDownTick=tick();
          vmLastX=ev.getX(0);
          vmLastY=ev.getY(0);
          vmIsScroll=(vmLastX>webLayout.getWidth()-dpx(50));
          if (vmMoveInterval!=null){
            cancelTimer(vmFlingInterval);
            vmFlingInterval=null;
          }
          if (vmWaitClick!=null){
            cancelTimer(vmWaitClick);
            vmWaitClick=null;
          }
          if (vmMoveInterval!=null) cancelTimer(vmMoveInterval);
          vmMoveInterval=new Timer();
          vmMoveInterval.scheduleAtFixedRate(new VmMoveTask(),0,8);
          break;
        case MotionEvent.ACTION_UP:
          if (vmMoveInterval!=null) {
            cancelTimer(vmMoveInterval);
            vmMoveInterval=null;
          }
          if (vmDowned){
            if (vmOnWaitClick){
              if(vmMoved){
                if (!(vmIsScroll||vmIs2Finger)){
                  Log.d(_TAG,"DRAG UP");
                  mouseEv(7,0,0,0,0,tick()-vmDragTick);
                  Log.d(_TAG,"MOUSE UP");
                  mouseEv(2,0,0,0,0,0);
                }
                vmOnWaitClick=false;
              }
              else if (!vmIsScroll){
                Log.d(_TAG,"MOUSE UP");
                mouseEv(2,0,0,0,0,0);
                Log.d(_TAG,"MOUSE DOWN");
                mouseEv(1,0,0,0,0,0);
                if (vmWaitClick!=null)
                  cancelTimer(vmWaitClick);
                vmWaitClick=new Timer();
                vmWaitClick.schedule(new VmWaitClickTask(),400);
              }
            }
            else{
              if (vmMoved){
                vmFlingScroll=(vmIsScroll||vmIs2Finger);
                if (vmVelocityX!=0f||vmVelocityY!=0f){
                  Log.d(_TAG,"FLING START");
                  if (vmFlingInterval!=null)
                    cancelTimer(vmFlingInterval);
                  vmFlingInterval=new Timer();
                  vmFlingInterval.scheduleAtFixedRate(new VmFlingTask(),0,16);
                }
              }
              else if (!vmIsScroll){
                if (!vmIs2Finger) {
                  Log.d(_TAG, "MOUSE DOWN");
                  mouseEv(1, 0, 0, 0, 0, 0);
                  vmOnWaitClick = true;
                  if (vmWaitClick!=null)
                    cancelTimer(vmWaitClick);
                  vmWaitClick=new Timer();
                  vmWaitClick.schedule(new VmWaitClickTask(), 400);
                }
                else{
                  Log.d(_TAG,"RIGHT MOUSE DOWN");
                  mouseEv(3,0,0,0,0,0);
                  Log.d(_TAG,"RIGHT MOUSE UP");
                  mouseEv(4,0,0,0,0,0);
                }
              }
            }
          }
          vmDowned=vmMoved=vmIsScroll=vmIs2Finger=false;
          break;
        case MotionEvent.ACTION_MOVE:
          vmCurrX+=ev.getX(0)-vmLastX;
          vmCurrY+=ev.getY(0)-vmLastY;
          vmLastX=ev.getX(0);
          vmLastY=ev.getY(0);
          break;
        default:
          if (!vmIs2Finger) {
            vmIs2Finger = (ev.getPointerCount() > 1);
          }
          Log.d(_TAG,ev.toString());
          Log.d(_TAG,"VAL = "+ev.getAction());
      }
      // Log.d(_TAG,ev.toString());
      return true;
    });
  }

  public void onSaveInstanceState(@NonNull Bundle outState)
  {
    webView.saveState(outState);
  }
  public void onRestoreInstanceState(Bundle savedInstanceState)
  {
    webView.restoreState(savedInstanceState);
  }
}

package com.amarullz.app.googledocs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class WebLayout extends RelativeLayout {
  public interface TouchListener {
    boolean touchEvent(MotionEvent ev);
  }

  public interface KeyListener {
    boolean keyEvent(KeyEvent ev);
  }

  private TouchListener touchListener=null;
  private KeyListener keyListener=null;

  public WebLayout(Context context) {
    super(context);
  }
  public WebLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  public WebLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setTouchListener(TouchListener listener){
    touchListener=listener;
  }

  public void setKeyListener(KeyListener listener){
    keyListener=listener;
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    if (touchListener!=null){
      if (touchListener.touchEvent(event))
        return true;
    }
    return super.dispatchTouchEvent(event);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (keyListener!=null){
      if (keyListener.keyEvent(event))
        return true;
    }
    return super.dispatchKeyEvent(event);
  }
}

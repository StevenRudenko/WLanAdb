package com.wlanadb.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class BlockingFrameLayout extends FrameLayout {

  private static final int FADE_FACTOR = 170;

  private final Paint mFadePaint = new Paint();

  public BlockingFrameLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public BlockingFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BlockingFrameLayout(Context context) {
    super(context);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (!isEnabled())
      return true;

    return super.dispatchTouchEvent(ev);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    if (!isEnabled()) {
      mFadePaint.setColor(Color.argb(FADE_FACTOR, 0, 0, 0));
      canvas.drawRect(0, 0, getWidth(), getHeight(), mFadePaint);
    }
  }
}

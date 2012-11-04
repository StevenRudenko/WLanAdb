package com.wlanadb.ui.prefs;

import android.content.Context;
import android.util.AttributeSet;

public class SwitchPreference extends android.preference.SwitchPreference {

  public SwitchPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public SwitchPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SwitchPreference(Context context) {
    super(context);
  }

  @Override
  protected void onClick() {
  }

}

package com.wlanadb.ui.prefs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class CheckBoxPreference extends android.preference.CheckBoxPreference {

  public CheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public CheckBoxPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CheckBoxPreference(Context context) {
    super(context);
  }

  @Override
  protected View onCreateView(ViewGroup parent) {
    final View v = super.onCreateView(parent);
    final CheckBox cb = (CheckBox) v.findViewById(android.R.id.checkbox);
    cb.setClickable(true);
    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        persistBoolean(isChecked);
      }
    });
    return v;
  }

  @Override
  protected void onClick() {
  }
}

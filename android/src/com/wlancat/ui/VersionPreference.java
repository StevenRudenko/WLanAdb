package com.wlancat.ui;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class VersionPreference extends Preference {

  public VersionPreference(Context context) {
    this(context, null);
  }

  public VersionPreference(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public VersionPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    
  }

  @Override
  protected void onBindView(View view) {
    super.onBindView(view);

    final TextView textView = (TextView) view.findViewById(android.R.id.title);
    textView.getLayoutParams().width = LayoutParams.MATCH_PARENT;
    textView.setGravity(Gravity.CENTER_HORIZONTAL);
    textView.setSingleLine(false);
    textView.setMaxLines(2);
    textView.setText("Version 0.1b\n(11:41bbc57a5c2b)");
    textView.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
  }
}

package com.wlancat.ui;

import com.wlancat.utils.HashHelper;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

public class PasswordPreference extends EditTextPreference {

  private String previousPassword = null;

  public PasswordPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public PasswordPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public PasswordPreference(Context context) {
    super(context);
  }

  @Override
  public void setText(String text) {
    final String hash = HashHelper.getHashString(text);
    super.setText(hash);
  }

  @Override
  protected void onClick() {
    previousPassword = getText();
    setText(null);
    super.onClick();
  }
  
  @Override
  protected void onDialogClosed(boolean positiveResult) {
    if (!positiveResult)
      super.setText(previousPassword);
    else if (TextUtils.isEmpty(getEditText().getText()))
      super.setText(null);
    super.onDialogClosed(positiveResult);
  }
}

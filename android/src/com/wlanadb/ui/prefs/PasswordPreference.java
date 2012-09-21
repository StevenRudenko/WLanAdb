package com.wlanadb.ui.prefs;

import com.wlanadb.R;
import com.wlanadb.utils.HashHelper;

import android.content.Context;
import android.preference.DialogPreference;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class PasswordPreference extends DialogPreference {

  private EditText viewPasswordText;
  private CheckBox viewPasswordShowCheck;

  public PasswordPreference(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PasswordPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, android.R.attr.editTextPreferenceStyle);

    setDialogLayoutResource(R.layout.preference_password);
    setPositiveButtonText(R.string.ok);
    setNegativeButtonText(R.string.cancel);
  }

  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);

    viewPasswordText = (EditText) view.findViewById(R.id.passwordText);
    viewPasswordShowCheck = (CheckBox) view.findViewById(R.id.passwordShowCheck);

    viewPasswordShowCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        viewPasswordText.setInputType(isChecked
            ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
      }
    });
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult) {
      final CharSequence text = viewPasswordText.getText();
      if (TextUtils.isEmpty(text)) {
        final String prevValue = getPersistedString(null);
        //HACK: we need this to launch preferences change update
        if (prevValue != null)
          persistString(null);
        else
          persistString("");
      } else {
        final String password = HashHelper.getHashString(text.toString());
        persistString(password);
      }
    }
  }

  @Override
  public boolean shouldCommit() {
    return true;
  }
}

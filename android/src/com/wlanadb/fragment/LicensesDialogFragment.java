package com.wlanadb.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.wlanadb.R;

public class LicensesDialogFragment extends DialogFragment {

  public static Dialog createDialog(Context context) {
    final LayoutInflater inflater = LayoutInflater.from(context);
    final View v = inflater.inflate(R.layout.fragment_licenses, null);

    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(R.string.pref_about_license_title);
    builder.setView(v);
    return builder.create();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return createDialog(getActivity());
  }
}

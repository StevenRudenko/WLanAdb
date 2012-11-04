package com.wlanadb.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.wlanadb.R;

public class EnableWifiDialogFragment extends DialogFragment {
  public static final int MSG_APP = R.string.dialog_enable_wifi_summary;
  public static final int MSG_TRUSTED_HOTSPOTS = R.string.dialog_enable_wifi_trusted_list_summary;
  
  private static final String ARG_MSG = ":msg";

  public static Dialog createDialog(final Context context, int msg) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(R.string.dialog_enable_wifi_title);
    builder.setMessage(msg);
    builder.setPositiveButton(R.string.dialog_enable_goto_wifi_settings, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        context.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
      }
    });
    builder.setNegativeButton(R.string.cancel, null);
    return builder.create();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final int msg;
    final Bundle args = getArguments();
    if (args == null)
      msg = MSG_APP;
    else
      msg = args.getInt(ARG_MSG, MSG_APP);
    return createDialog(getActivity(), msg);
  }
}

package com.wlancat.fragment;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wlancat.R;
import com.wlancat.data.LogcatLine;
import com.wlancat.logcat.LogReader;
import com.wlancat.logcat.LogReader.OnLogMessageListener;
import com.wlancat.logcat.PidsController;
import com.wlancat.logcat.PidsController.OnPidsUpdateListener;
import com.wlancat.ui.LogcatAdapter;
import com.wlancat.ui.PidsAdapter;
import com.wlancat.utils.AndroidUtils.RunningProcess;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

public class LogcatFragment extends Fragment implements OnLogMessageListener, OnPidsUpdateListener, CompoundButton.OnCheckedChangeListener {
  @SuppressWarnings("unused")
  private static final String TAG = LogcatFragment.class.getSimpleName();

  private final LogReader mLogReader = new LogReader(this);
  private PidsController mPidsController;
  private final Pattern mLogLinePattern = Pattern.compile("^([A-Z])\\/(.*)\\(\\s*(\\d+)\\s*\\): (.*)$");

  private PidsAdapter mPidsAdapter;
  private LogcatAdapter mLogsAdapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final Context context = getActivity();

    final View v = inflater.inflate(R.layout.fragment_logcat, container, false);

    final View viewEmpty = v.findViewById(android.R.id.empty);
    final ListView viewList = (ListView) v.findViewById(R.id.logcatList);
    viewList.setEmptyView(viewEmpty);

    mLogsAdapter = new LogcatAdapter(context);
    viewList.setAdapter(mLogsAdapter);

    // logcat filter actions section
    mPidsController = new PidsController(context, this);
    mPidsAdapter = new PidsAdapter(context);
    final ListView viewPidsList = (ListView) v.findViewById(R.id.logcatFiltersPidsList);
    viewPidsList.setAdapter(mPidsAdapter);

    final CheckBox viewFilterTypeV = (CheckBox) v.findViewById(R.id.logcatFilterTypeV);
    viewFilterTypeV.setOnCheckedChangeListener(this);
    final CheckBox viewFilterTypeD = (CheckBox) v.findViewById(R.id.logcatFilterTypeD);
    viewFilterTypeD.setOnCheckedChangeListener(this);
    final CheckBox viewFilterTypeI = (CheckBox) v.findViewById(R.id.logcatFilterTypeI);
    viewFilterTypeI.setOnCheckedChangeListener(this);
    final CheckBox viewFilterTypeW = (CheckBox) v.findViewById(R.id.logcatFilterTypeW);
    viewFilterTypeW.setOnCheckedChangeListener(this);
    final CheckBox viewFilterTypeE = (CheckBox) v.findViewById(R.id.logcatFilterTypeE);
    viewFilterTypeE.setOnCheckedChangeListener(this);
    final CheckBox viewFilterTypeA = (CheckBox) v.findViewById(R.id.logcatFilterTypeA);
    viewFilterTypeA.setOnCheckedChangeListener(this);

    final CheckBox viewFilterHideSytemPids = (CheckBox) v.findViewById(R.id.logcatFilterHideSystemPids);
    viewFilterHideSytemPids.setOnCheckedChangeListener(this);

    return v;
  }

  @Override
  public void onResume() {
    mLogReader.startOnNewTread();
    mPidsController.start();

    super.onResume();
  }

  @Override
  public void onPause() {
    mLogReader.stop();
    mPidsController.stop();

    super.onPause();
  }

  @Override
  public void onLogMessage(String message) {
    final Matcher matcher = mLogLinePattern.matcher(message);
    if (!matcher.matches())
      return;

    final LogcatLine logcatLine = new LogcatLine();
    logcatLine.type = matcher.group(1);
    logcatLine.tag = matcher.group(2);
    logcatLine.pid = Integer.parseInt(matcher.group(3));
    logcatLine.text = matcher.group(4);
    mLogsAdapter.add(logcatLine);
  }

  @Override
  public void onPidsUpdated(Collection<RunningProcess> pids) {
    mPidsAdapter.setItems(pids);
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    final int id = buttonView.getId();
    switch (id) {
    case R.id.logcatFilterTypeV:
      mLogsAdapter.getFilter().setType(LogcatAdapter.LogcatFilter.TYPE_V, isChecked).filter();
      break;
    case R.id.logcatFilterTypeD:
      mLogsAdapter.getFilter().setType(LogcatAdapter.LogcatFilter.TYPE_D, isChecked).filter();
      break;
    case R.id.logcatFilterTypeI:
      mLogsAdapter.getFilter().setType(LogcatAdapter.LogcatFilter.TYPE_I, isChecked).filter();
      break;
    case R.id.logcatFilterTypeW:
      mLogsAdapter.getFilter().setType(LogcatAdapter.LogcatFilter.TYPE_W, isChecked).filter();
      break;
    case R.id.logcatFilterTypeE:
      mLogsAdapter.getFilter().setType(LogcatAdapter.LogcatFilter.TYPE_E, isChecked).filter();
      break;
    case R.id.logcatFilterTypeA:
      mLogsAdapter.getFilter().setType(LogcatAdapter.LogcatFilter.TYPE_A, isChecked).filter();
      break;

    case R.id.logcatFilterHideSystemPids:
      mPidsAdapter.getFilter().setHideSystemApps(isChecked).filter();
      break;

    default:
      break;
    }

  }
}

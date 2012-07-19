package com.wlancat.fragment;

import java.util.Collection;

import com.wlancat.R;
import com.wlancat.data.LogcatLine;
import com.wlancat.logcat.LogFilter;
import com.wlancat.logcat.LogParser;
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

  private final LogFilter mLogFilter = new LogFilter();
  private PidsController mPidsController;

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
    mLogsAdapter.getFilter().setLogFilter(mLogFilter);
    viewList.setAdapter(mLogsAdapter);

    // logcat filter actions section
    mPidsController = new PidsController(context);
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
    mPidsController.addOnPidsUpdateListener(this);
    mPidsController.addOnPidsUpdateListener(mLogFilter);
    mPidsController.start();

    super.onResume();
  }

  @Override
  public void onPause() {
    mLogReader.stop();
    mPidsController.removeOnPidsUpdateListener(this);
    mPidsController.removeOnPidsUpdateListener(mLogFilter);
    mPidsController.stop();

    super.onPause();
  }

  @Override
  public void onLogMessage(String message) {
    final LogcatLine logcatLine = LogParser.parse(message);
    if (logcatLine == null)
      return;

    mLogsAdapter.add(logcatLine);
  }

  @Override
  public void onPidsUpdated(Collection<RunningProcess> processes) {
    mPidsAdapter.setItems(processes);
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    final int id = buttonView.getId();
    switch (id) {
    case R.id.logcatFilterTypeV:
      mLogFilter.setType(LogFilter.TYPE_V, isChecked);
      mLogsAdapter.getFilter().filter();
      break;
    case R.id.logcatFilterTypeD:
      mLogFilter.setType(LogFilter.TYPE_D, isChecked);
      mLogsAdapter.getFilter().filter();
      break;
    case R.id.logcatFilterTypeI:
      mLogFilter.setType(LogFilter.TYPE_I, isChecked);
      mLogsAdapter.getFilter().filter();
      break;
    case R.id.logcatFilterTypeW:
      mLogFilter.setType(LogFilter.TYPE_W, isChecked);
      mLogsAdapter.getFilter().filter();
      break;
    case R.id.logcatFilterTypeE:
      mLogFilter.setType(LogFilter.TYPE_E, isChecked);
      mLogsAdapter.getFilter().filter();
      break;
    case R.id.logcatFilterTypeA:
      mLogFilter.setType(LogFilter.TYPE_A, isChecked);
      mLogsAdapter.getFilter().filter();
      break;

    case R.id.logcatFilterHideSystemPids:
      mPidsAdapter.getFilter().setHideSystemApps(isChecked).filter();
      break;

    default:
      break;
    }

  }
}

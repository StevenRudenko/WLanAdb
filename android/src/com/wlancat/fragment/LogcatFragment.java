package com.wlancat.fragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wlancat.R;
import com.wlancat.data.LogcatLine;
import com.wlancat.logcat.LogReader;
import com.wlancat.logcat.LogReader.OnLogMessageListener;
import com.wlancat.ui.LogcatAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class LogcatFragment extends Fragment implements OnLogMessageListener {

  private final LogReader mLogReader = new LogReader(this);
  private final Pattern mLogLinePattern = Pattern.compile("^([A-Z])\\/(.*)\\(\\s*(\\d+)\\s*\\): (.*)$");

  private LogcatAdapter mAdapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.fragment_logcat, container, false);

    final View viewEmpty = v.findViewById(android.R.id.empty);
    final ListView viewList = (ListView) v.findViewById(R.id.logcatList);
    viewList.setEmptyView(viewEmpty);

    mAdapter = new LogcatAdapter(getActivity());
    viewList.setAdapter(mAdapter);

    return v;
  }

  @Override
  public void onResume() {
    mLogReader.startOnTread();
    super.onResume();
  }

  @Override
  public void onPause() {
    mLogReader.stop();
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
    logcatLine.process = matcher.group(3);
    logcatLine.text = matcher.group(4);
    mAdapter.add(logcatLine);
  }

}

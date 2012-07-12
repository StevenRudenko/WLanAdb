package com.wlancat.fragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.signalslot_apt.SignalSlot;
import net.sf.signalslot_apt.annotations.signalslot;
import net.sf.signalslot_apt.annotations.slot;

import com.wlancat.data.LogcatLine;
import com.wlancat.logcat.LogReader;
import com.wlancat.logcat.LogReaderSignalSlot;
import com.wlancat.ui.LogcatAdapter;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

@signalslot
public class LogcatFragment extends ListFragment {

  private final LogReader mLogReader = new LogReaderSignalSlot();
  private final Pattern mLogLinePattern = Pattern.compile("^([A-Z])\\/(.*)\\(\\s*(\\d+)\\s*\\): (.*)$");

  private LogcatAdapter mAdapter;

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    final ListView viewList = getListView();
    viewList.setStackFromBottom(true);
    viewList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
    viewList.setFastScrollEnabled(true);

    mAdapter = new LogcatAdapter(getActivity());
    setListAdapter(mAdapter);
  }

  @Override
  public void onResume() {
    SignalSlot.connect(mLogReader, LogReaderSignalSlot.Signals.ONLOGMESSAGE_STRING, this, LogcatFragmentSignalSlot.Slots.PARSELINE_STRING);
    mLogReader.startOnTread();
    super.onResume();
  }

  @Override
  public void onPause() {
    mLogReader.stop();
    super.onPause();
  }

  @slot
  public void parseLine(String line) {
    final Matcher matcher = mLogLinePattern.matcher(line);
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

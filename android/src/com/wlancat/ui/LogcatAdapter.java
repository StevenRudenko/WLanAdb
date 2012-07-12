package com.wlancat.ui;

import java.util.ArrayList;

import com.wlancat.data.LogcatLine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LogcatAdapter extends BaseAdapter {

  private static final int MSG_UPDATE_LIST = 1;
  private static final int MSG_UPDATE_LIST_DELAY = 250;

  private final Context mContext;
  private final ArrayList<LogcatLine> mItems = new ArrayList<LogcatLine>();
  private final ArrayList<LogcatLine> mCache = new ArrayList<LogcatLine>();

  @SuppressLint("HandlerLeak")
  private final Handler mFirstTimeListUpdater = new Handler() {
    private static final int COUNT_THRESHOLD = 1;
    private int lastCount = 0;

    public void handleMessage(android.os.Message msg) {
      final int count = mCache.size();
      if (count - lastCount <= COUNT_THRESHOLD) {
        mItems.addAll(mCache);
        notifyDataSetChanged();

        mCache.clear();
        lastCount = 0;
        removeMessages(MSG_UPDATE_LIST);
        return;
      }

      lastCount = count;
      sendEmptyMessageDelayed(MSG_UPDATE_LIST, MSG_UPDATE_LIST_DELAY);
    };
  };

  public LogcatAdapter(Context context) {
    this.mContext = context;
  }

  public void add(LogcatLine item) {
    if (mCache.isEmpty() && !mFirstTimeListUpdater.hasMessages(MSG_UPDATE_LIST)) {
      mFirstTimeListUpdater.sendEmptyMessageDelayed(MSG_UPDATE_LIST, MSG_UPDATE_LIST_DELAY);
    }
    mCache.add(item);
  }

  @Override
  public int getCount() {
    return mItems.size();
  }

  @Override
  public LogcatLine getItem(int position) {
    return mItems.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final TextView logText;
    if (convertView == null) {
      logText = new TextView(mContext);
      convertView = logText;
    } else {
      logText = (TextView) convertView;
    }

    final LogcatLine item = getItem(position);
    logText.setText(item.text);

    return convertView;
  }
}

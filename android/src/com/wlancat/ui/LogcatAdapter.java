package com.wlancat.ui;

import java.util.ArrayList;

import com.wlancat.R;
import com.wlancat.data.LogcatLine;
import com.wlancat.logcat.LogFilter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class LogcatAdapter extends BaseAdapter implements Filterable {
  @SuppressWarnings("unused")
  private final static String TAG = LogcatAdapter.class.getSimpleName();

  private final LayoutInflater mInflater;

  private final ArrayList<LogcatLine> mOriginalValues = new ArrayList<LogcatLine>();
  private ArrayList<LogcatLine> mObjects = new ArrayList<LogcatLine>();

  private final LogcatFilter mFilter = new LogcatFilter();

  @SuppressLint("HandlerLeak")
  private final FirstTimeLoadUpdater mFirstTimeLoadUpdater = new FirstTimeLoadUpdater();

  public LogcatAdapter(Context context) {
    mInflater = LayoutInflater.from(context);
  }

  public void add(LogcatLine item) {
    synchronized (mOriginalValues) {
      if (mOriginalValues.isEmpty()) {
        mFirstTimeLoadUpdater.add(item);
      } else {
        mOriginalValues.add(item);
        getFilter().filter();
      }
    }
  }

  @Override
  public int getCount() {
    return mObjects.size();
  }

  @Override
  public LogcatLine getItem(int position) {
    return mObjects.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final ViewHolder holder;
    if (convertView == null) {
      holder = new ViewHolder();
      convertView = mInflater.inflate(R.layout.logcat_list_item, parent, false);
      holder.viewType = (TextView) convertView.findViewById(R.id.logcatType);
      holder.viewTag = (TextView) convertView.findViewById(R.id.logcatTag);
      holder.viewText = (TextView) convertView.findViewById(R.id.logcatText);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    final LogcatLine item = getItem(position);
    holder.viewType.setText(item.type);
    holder.viewTag.setText(item.tag);
    holder.viewText.setText(item.text);

    return convertView;
  }

  @Override
  public LogcatFilter getFilter() {
    return mFilter;
  }

  public class LogcatFilter extends Filter {
    private LogFilter mFilter = null;

    public void setLogFilter(LogFilter filter) {
      mFilter = filter;
    }

    public void filter() {
      filter(null);
    }

    @Override
    protected FilterResults performFiltering(CharSequence prefix) {
      final FilterResults results = new FilterResults();

      ArrayList<LogcatLine> values;
      synchronized (mOriginalValues) {
        values = new ArrayList<LogcatLine>(mOriginalValues);
      }

      final int count = values.size();
      final ArrayList<LogcatLine> newValues = new ArrayList<LogcatLine>();

      if (mFilter == null) {
        results.values = values;
        results.count = values.size();
        return results;
      }

      if (mFilter.filterAll()) {
        results.values = newValues;
        results.count = 0;
        return results;
      }

      for (int i = 0; i < count; ++i) {
        final LogcatLine value = values.get(i);
        if (mFilter.filter(value))
          continue;

        newValues.add(value);
      }

      results.values = newValues;
      results.count = newValues.size();

      return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      //noinspection unchecked
      mObjects = (ArrayList<LogcatLine>) results.values;
      if (results.count > 0) {
        notifyDataSetChanged();
      } else {
        notifyDataSetInvalidated();
      }
    }
  }

  private static class ViewHolder {
    TextView viewType;
    TextView viewTag;
    TextView viewText;
  }

  private class FirstTimeLoadUpdater extends Handler {
    private static final int MSG_UPDATE_LIST = 1;
    private static final int MSG_UPDATE_LIST_DELAY = 350;

    private static final int COUNT_THRESHOLD = 2;

    private final ArrayList<LogcatLine> mFirstTimeLoadCache = new ArrayList<LogcatLine>();

    private int lastCount = 0;

    public void add(LogcatLine item) {
      mFirstTimeLoadCache.add(item);

      if (!mFirstTimeLoadUpdater.hasMessages(MSG_UPDATE_LIST))
        mFirstTimeLoadUpdater.sendEmptyMessage(MSG_UPDATE_LIST);
    }

    public void handleMessage(android.os.Message msg) {
      final int count = mFirstTimeLoadCache.size();
      if (count - lastCount < COUNT_THRESHOLD) {
        removeMessages(MSG_UPDATE_LIST);

        synchronized (mOriginalValues) {
          mOriginalValues.addAll(mFirstTimeLoadCache);
          mFirstTimeLoadCache.clear();
          lastCount = 0;
        }

        getFilter().filter();
        return;
      }

      lastCount = count;
      sendEmptyMessageDelayed(MSG_UPDATE_LIST, MSG_UPDATE_LIST_DELAY);
    };
  }
}

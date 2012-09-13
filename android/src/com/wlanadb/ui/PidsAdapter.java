package com.wlanadb.ui;

import java.util.ArrayList;
import java.util.Collection;

import com.wlanadb.utils.AndroidUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class PidsAdapter extends BaseAdapter implements Filterable {

  private final Context mContext;
  private final LayoutInflater mInflater;

  private final ArrayList<AndroidUtils.RunningProcess> mOriginalValues = new ArrayList<AndroidUtils.RunningProcess>();
  private ArrayList<AndroidUtils.RunningProcess> mObjects = new ArrayList<AndroidUtils.RunningProcess>();

  private final SystemAppsFilter mFilter = new SystemAppsFilter();

  public PidsAdapter(Context context) {
    this.mContext = context;
    mInflater = LayoutInflater.from(context);
  }

  public void setItems(Collection<AndroidUtils.RunningProcess> items) {
    mOriginalValues.clear();
    if (items != null)
      mOriginalValues.addAll(items);

    getFilter().filter();
  }

  @Override
  public int getCount() {
    return mObjects.size();
  }

  @Override
  public AndroidUtils.RunningProcess getItem(int position) {
    return mObjects.get(position);
  }

  @Override
  public long getItemId(int position) {
    return mObjects.get(position).pid;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final TextView viewText;
    if (convertView == null) {
      viewText = new TextView(mContext);
      convertView = viewText;
    } else {
      viewText = (TextView) convertView;
    }

    final AndroidUtils.RunningProcess item = getItem(position);
    viewText.setText(item.name);

    return convertView;
  }

  @Override
  public SystemAppsFilter getFilter() {
    return mFilter;
  }

  public class SystemAppsFilter extends Filter {
    private static final int MIN_USER_UID = 10000;

    private boolean mHideSystemApps = true;

    public SystemAppsFilter setHideSystemApps(boolean hide) {
      mHideSystemApps = hide;
      return this;
    }

    public void filter() {
      filter(null);
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      final FilterResults results = new FilterResults();

      ArrayList<AndroidUtils.RunningProcess> values;
      synchronized (mOriginalValues) {
        values = new ArrayList<AndroidUtils.RunningProcess>(mOriginalValues);
      }

      if (!mHideSystemApps) {
        results.values = values;
        results.count = values.size();
        return results;
      }

      final int count = values.size();
      final ArrayList<AndroidUtils.RunningProcess> newValues = new ArrayList<AndroidUtils.RunningProcess>();

      for (int i = 0; i < count; ++i) {
        final AndroidUtils.RunningProcess value = values.get(i);
        if (value.uid >= MIN_USER_UID)
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
      mObjects = (ArrayList<AndroidUtils.RunningProcess>) results.values;
      if (results.count > 0) {
        notifyDataSetChanged();
      } else {
        notifyDataSetInvalidated();
      }
    }

  }
}
